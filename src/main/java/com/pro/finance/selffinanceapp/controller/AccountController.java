package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.AccountDTO;
import com.pro.finance.selffinanceapp.dto.AccountTransactionDTO;
import com.pro.finance.selffinanceapp.dto.TransferRequestDTO;
import com.pro.finance.selffinanceapp.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // ── ACCOUNTS ──

    @GetMapping
    public ResponseEntity<?> getAccounts(Principal principal) {
        try {
            List<AccountDTO> accounts = accountService.getAccounts(principal.getName());
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addAccount(@RequestBody AccountDTO dto, Principal principal) {
        try {
            return ResponseEntity.ok(accountService.addAccount(principal.getName(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable Long id,
                                           @RequestBody AccountDTO dto,
                                           Principal principal) {
        try {
            return ResponseEntity.ok(accountService.updateAccount(id, principal.getName(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id, Principal principal) {
        try {
            accountService.deleteAccount(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // ── TRANSACTIONS ──

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable Long id,
                                             @RequestParam(required = false) String month,
                                             Principal principal) {
        try {
            return ResponseEntity.ok(accountService.getTransactions(id, principal.getName(), month));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> addTransaction(@RequestBody AccountTransactionDTO dto,
                                            Principal principal) {
        try {
            return ResponseEntity.ok(accountService.addTransaction(principal.getName(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/transactions/{txId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long txId, Principal principal) {
        try {
            accountService.deleteTransaction(txId, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // ── TRANSFER ──

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequestDTO dto, Principal principal) {
        try {
            accountService.transfer(principal.getName(), dto);
            return ResponseEntity.ok("Transfer successful");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}