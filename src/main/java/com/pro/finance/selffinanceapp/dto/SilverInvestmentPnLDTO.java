package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SilverInvestmentPnLDTO {
    private Long id;
    private BigDecimal grams;
    private BigDecimal purchasePrice;     // pricePerGram at buy time
    private LocalDate purchaseDate;
    private BigDecimal investedAmount;    // grams × purchasePrice
    private BigDecimal currentPricePerGram;
    private BigDecimal currentValue;      // grams × currentPrice
    private BigDecimal profitLoss;        // currentValue - investedAmount
    private BigDecimal profitLossPercent;
}