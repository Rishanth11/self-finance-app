package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoldHistoryDTO {

    private Long id;
    private String purchaseDate;
    private BigDecimal gramsPurchased;
    private BigDecimal purchasePricePerGram;
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal profit;

}