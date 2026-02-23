package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class GoldSummaryDTO {
    private double totalGrams;
    private double totalInvested;
    private double livePrice;
    private double currentValue;
    private double profitLoss;
}
