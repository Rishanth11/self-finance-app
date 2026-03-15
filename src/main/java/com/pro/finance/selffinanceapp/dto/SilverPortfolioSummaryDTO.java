package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class SilverPortfolioSummaryDTO {
    private BigDecimal totalGrams;
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercent;
    private List<SilverInvestmentPnLDTO> investments;
}