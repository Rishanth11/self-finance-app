package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetSummaryDTO {
    private Long budgetId;
    private String budgetName;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private Double spentPercentage;
    private List<CategorySummaryDTO> categorySummaries;
    private long unreadAlerts;
}