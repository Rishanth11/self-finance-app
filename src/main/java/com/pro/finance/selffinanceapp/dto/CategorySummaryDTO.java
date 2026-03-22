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
public class CategorySummaryDTO {
    private Long categoryId;
    private String categoryName;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double spentPercentage;
    private String status; // SAFE, WARNING, EXCEEDED
}