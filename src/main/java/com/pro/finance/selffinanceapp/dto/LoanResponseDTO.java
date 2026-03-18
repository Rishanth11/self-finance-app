package com.pro.finance.selffinanceapp.dto;

import com.pro.finance.selffinanceapp.model.LoanType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LoanResponseDTO {

    private Long      id;
    private Long      userId;
    private String    loanName;
    private LoanType  loanType;

    private double    principal;
    private double    interestRate;
    private int       tenureMonths;

    // Calculated fields
    private double    emiAmount;
    private double    totalInterest;
    private double    totalPayable;
    private double    outstandingBalance;

    private LocalDate startDate;
    private boolean   closed;
}