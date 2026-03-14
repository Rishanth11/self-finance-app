package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoldSummaryDTO {

    private BigDecimal totalGrams;
    private BigDecimal totalInvested;
    private BigDecimal livePrice;
    private BigDecimal currentValue;
    private BigDecimal profitLoss;

}