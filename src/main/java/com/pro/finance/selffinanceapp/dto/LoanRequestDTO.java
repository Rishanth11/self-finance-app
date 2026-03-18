package com.pro.finance.selffinanceapp.dto;

import com.pro.finance.selffinanceapp.model.LoanType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoanRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Loan name is required")
    private String loanName;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @Positive(message = "Principal must be positive")
    private double principal;

    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private double interestRate;   // annual %

    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 360, message = "Tenure cannot exceed 360 months (30 years)")
    private int tenureMonths;
}