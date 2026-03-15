package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockHoldingDTO {
    private Long id;
    private String symbol;
    private String exchange;
    private String companyName;
    private String sector;
    private Integer quantity;
    private BigDecimal avgBuyPrice;
    private LocalDate buyDate;
}