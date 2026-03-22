package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetExpenseRequestDTO {
    private Long budgetCategoryId;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
}