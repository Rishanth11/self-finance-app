package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetCategoryDTO {
    private String categoryName;
    private BigDecimal allocatedAmount;
    private Integer alertThreshold;
}