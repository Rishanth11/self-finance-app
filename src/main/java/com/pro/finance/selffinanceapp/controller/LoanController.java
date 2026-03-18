package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.LoanRequestDTO;
import com.pro.finance.selffinanceapp.dto.LoanResponseDTO;
import com.pro.finance.selffinanceapp.dto.LoanSummaryDTO;
import com.pro.finance.selffinanceapp.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    // ➕ ADD LOAN
    @PostMapping
    public ResponseEntity<LoanResponseDTO> addLoan(@Valid @RequestBody LoanRequestDTO request) {
        LoanResponseDTO response = loanService.addLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 📋 GET ALL LOANS FOR USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponseDTO>> getLoansByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUser(userId));
    }

    // 🔍 GET SINGLE LOAN
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanById(loanId));
    }

    // 📊 GET LOAN SUMMARY (for dashboard + chart)
    @GetMapping("/{loanId}/summary")
    public ResponseEntity<LoanSummaryDTO> getLoanSummary(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.getLoanSummary(loanId));
    }

    // 🗑️ DELETE LOAN
    @DeleteMapping("/{loanId}")
    public ResponseEntity<String> deleteLoan(@PathVariable Long loanId) {
        loanService.deleteLoan(loanId);
        return ResponseEntity.ok("Loan and its EMI schedule deleted successfully");
    }
}