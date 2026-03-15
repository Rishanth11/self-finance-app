package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Account;
import com.pro.finance.selffinanceapp.model.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findByAccountOrderByDateDesc(Account account);

    List<AccountTransaction> findByAccountAndDateBetweenOrderByDateDesc(
            Account account, LocalDate start, LocalDate end);

    List<AccountTransaction> findByTransferPairId(Long transferPairId);
}