package com.pro.finance.selffinanceapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanSummaryDTO {

    private Long   loanId;
    private String loanName;

    // Progress
    private int    totalMonths;
    private int    paidMonths;
    private int    remainingMonths;
    private double progressPercent;   // 0–100

    // Financials
    private double principal;
    private double totalPayable;
    private double totalInterest;

    private double totalPrincipalPaid;
    private double totalInterestPaid;
    private double totalAmountPaid;

    private double outstandingBalance;

    // Chart data — parallel arrays, index = month number
    private double[] principalComponents;    // principal paid per EMI
    private double[] interestComponents;     // interest paid per EMI
    private double[] remainingBalances;      // balance after each EMI
}