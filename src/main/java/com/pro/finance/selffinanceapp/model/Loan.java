package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "loan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    // ── New descriptive fields ──────────────────────────────────
    private String loanName;       // e.g. "Home Loan - SBI"

    @Enumerated(EnumType.STRING)
    private LoanType loanType;     // HOME, CAR, PERSONAL, EDUCATION, OTHER

    // ── Core loan fields ────────────────────────────────────────
    private double principal;
    private double interestRate;   // annual %
    private int    tenureMonths;

    // ── Calculated fields (set by service, not by user) ─────────
    private double emiAmount;
    private double totalInterest;
    private double totalPayable;
    private double outstandingBalance;

    private LocalDate startDate;

    @Builder.Default
    private boolean closed = false;
}