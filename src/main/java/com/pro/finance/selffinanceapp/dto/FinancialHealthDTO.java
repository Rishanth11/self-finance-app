package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FinancialHealthDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal savings;
    private int score;
    private String status;
}