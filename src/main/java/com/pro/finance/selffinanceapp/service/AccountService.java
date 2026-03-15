package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.AccountDTO;
import com.pro.finance.selffinanceapp.dto.AccountTransactionDTO;
import com.pro.finance.selffinanceapp.dto.TransferRequestDTO;
import com.pro.finance.selffinanceapp.model.Account;
import com.pro.finance.selffinanceapp.model.AccountTransaction;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.AccountRepository;
import com.pro.finance.selffinanceapp.repository.AccountTransactionRepository;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // ── GET ALL ACCOUNTS ──
    public List<AccountDTO> getAccounts(String username) {
        User user = getUser(username);
        return accountRepository.findByUser(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── ADD ACCOUNT ──
    public AccountDTO addAccount(String username, AccountDTO dto) {
        User user = getUser(username);

        Account account = new Account();
        account.setName(dto.getName());
        account.setType(Account.AccountType.valueOf(dto.getType()));
        account.setOpeningBalance(dto.getOpeningBalance() != null ? dto.getOpeningBalance() : BigDecimal.ZERO);
        account.setBalance(dto.getOpeningBalance() != null ? dto.getOpeningBalance() : BigDecimal.ZERO);
        account.setUser(user);

        return toDTO(accountRepository.save(account));
    }

    // ── EDIT ACCOUNT ──
    public AccountDTO updateAccount(Long id, String username, AccountDTO dto) {
        Account account = getAccountForUser(id, username);
        account.setName(dto.getName());
        account.setType(Account.AccountType.valueOf(dto.getType()));

        // Adjust balance by difference in opening balance
        BigDecimal oldOpening = account.getOpeningBalance();
        BigDecimal newOpening = dto.getOpeningBalance() != null ? dto.getOpeningBalance() : BigDecimal.ZERO;
        BigDecimal diff = newOpening.subtract(oldOpening);
        account.setOpeningBalance(newOpening);
        account.setBalance(account.getBalance().add(diff));

        return toDTO(accountRepository.save(account));
    }

    // ── DELETE ACCOUNT ──
    public void deleteAccount(Long id, String username) {
        Account account = getAccountForUser(id, username);
        transactionRepository.findByAccountOrderByDateDesc(account)
                .forEach(transactionRepository::delete);
        accountRepository.delete(account);
    }

    // ── ADD TRANSACTION (CREDIT or DEBIT) ──
    public AccountTransactionDTO addTransaction(String username, AccountTransactionDTO dto) {
        Account account = getAccountForUser(dto.getAccountId(), username);

        AccountTransaction tx = new AccountTransaction();
        tx.setAmount(dto.getAmount());
        tx.setType(AccountTransaction.TransactionType.valueOf(dto.getType()));
        tx.setDescription(dto.getDescription());
        tx.setDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());
        tx.setAccount(account);

        // Update account balance
        if (tx.getType() == AccountTransaction.TransactionType.CREDIT) {
            // For credit card: credit = paying off debt (balance goes down)
            // For bank/upi: credit = money in (balance goes up)
            if (account.getType() == Account.AccountType.CREDIT_CARD) {
                account.setBalance(account.getBalance().subtract(dto.getAmount()));
            } else {
                account.setBalance(account.getBalance().add(dto.getAmount()));
            }
        } else if (tx.getType() == AccountTransaction.TransactionType.DEBIT) {
            // For credit card: debit = spending (debt goes up)
            // For bank/upi: debit = money out (balance goes down)
            if (account.getType() == Account.AccountType.CREDIT_CARD) {
                account.setBalance(account.getBalance().add(dto.getAmount()));
            } else {
                account.setBalance(account.getBalance().subtract(dto.getAmount()));
            }
        }

        accountRepository.save(account);
        return toTxDTO(transactionRepository.save(tx));
    }

    // ── TRANSFER BETWEEN ACCOUNTS ──
    @Transactional
    public void transfer(String username, TransferRequestDTO dto) {
        Account from = getAccountForUser(dto.getFromAccountId(), username);
        Account to   = getAccountForUser(dto.getToAccountId(), username);

        if (dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than 0");
        }

        LocalDate date = dto.getDate() != null ? dto.getDate() : LocalDate.now();
        String desc = dto.getDescription() != null ? dto.getDescription() : "Transfer";

        // Deduct from source
        from.setBalance(from.getBalance().subtract(dto.getAmount()));
        accountRepository.save(from);

        // Add to destination
        to.setBalance(to.getBalance().add(dto.getAmount()));
        accountRepository.save(to);

        // Create TRANSFER_OUT transaction
        AccountTransaction txOut = new AccountTransaction();
        txOut.setAmount(dto.getAmount());
        txOut.setType(AccountTransaction.TransactionType.TRANSFER_OUT);
        txOut.setDescription(desc + " → " + to.getName());
        txOut.setDate(date);
        txOut.setAccount(from);
        AccountTransaction savedOut = transactionRepository.save(txOut);

        // Create TRANSFER_IN transaction
        AccountTransaction txIn = new AccountTransaction();
        txIn.setAmount(dto.getAmount());
        txIn.setType(AccountTransaction.TransactionType.TRANSFER_IN);
        txIn.setDescription(desc + " ← " + from.getName());
        txIn.setDate(date);
        txIn.setAccount(to);
        txIn.setTransferPairId(savedOut.getId());
        AccountTransaction savedIn = transactionRepository.save(txIn);

        // Link the pair
        savedOut.setTransferPairId(savedIn.getId());
        transactionRepository.save(savedOut);
    }

    // ── GET TRANSACTIONS FOR ACCOUNT ──
    public List<AccountTransactionDTO> getTransactions(Long accountId, String username,
                                                       String month) {
        Account account = getAccountForUser(accountId, username);

        List<AccountTransaction> txList;

        if (month != null && !month.isEmpty()) {
            String[] parts = month.split("-");
            int year  = Integer.parseInt(parts[0]);
            int mon   = Integer.parseInt(parts[1]);
            LocalDate start = LocalDate.of(year, mon, 1);
            LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());
            txList = transactionRepository
                    .findByAccountAndDateBetweenOrderByDateDesc(account, start, end);
        } else {
            txList = transactionRepository.findByAccountOrderByDateDesc(account);
        }

        return txList.stream().map(this::toTxDTO).collect(Collectors.toList());
    }

    // ── DELETE TRANSACTION ──
    public void deleteTransaction(Long txId, String username) {
        AccountTransaction tx = transactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Verify ownership
        getAccountForUser(tx.getAccount().getId(), username);

        // Reverse the balance effect
        Account account = tx.getAccount();
        reverseBalance(account, tx);
        accountRepository.save(account);

        // If it's a transfer, delete the pair too
        if (tx.getTransferPairId() != null) {
            transactionRepository.findById(tx.getTransferPairId()).ifPresent(pair -> {
                reverseBalance(pair.getAccount(), pair);
                accountRepository.save(pair.getAccount());
                transactionRepository.delete(pair);
            });
        }

        transactionRepository.delete(tx);
    }

    // ── HELPERS ──
    private void reverseBalance(Account account, AccountTransaction tx) {
        switch (tx.getType()) {
            case CREDIT -> {
                if (account.getType() == Account.AccountType.CREDIT_CARD) {
                    account.setBalance(account.getBalance().add(tx.getAmount()));
                } else {
                    account.setBalance(account.getBalance().subtract(tx.getAmount()));
                }
            }
            case DEBIT -> {
                if (account.getType() == Account.AccountType.CREDIT_CARD) {
                    account.setBalance(account.getBalance().subtract(tx.getAmount()));
                } else {
                    account.setBalance(account.getBalance().add(tx.getAmount()));
                }
            }
            case TRANSFER_IN  -> account.setBalance(account.getBalance().subtract(tx.getAmount()));
            case TRANSFER_OUT -> account.setBalance(account.getBalance().add(tx.getAmount()));
        }
    }

    private Account getAccountForUser(Long accountId, String username) {
        User user = getUser(username);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        return account;
    }

    private User getUser(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private AccountDTO toDTO(Account a) {
        return new AccountDTO(a.getId(), a.getName(),
                a.getType().name(), a.getOpeningBalance(), a.getBalance());
    }

    private AccountTransactionDTO toTxDTO(AccountTransaction tx) {
        return new AccountTransactionDTO(
                tx.getId(), tx.getAmount(), tx.getType().name(),
                tx.getDescription(), tx.getDate(),
                tx.getAccount().getId(), tx.getAccount().getName());
    }
}