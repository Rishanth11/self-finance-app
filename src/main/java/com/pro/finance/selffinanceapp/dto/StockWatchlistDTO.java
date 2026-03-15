package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockWatchlistDTO {
    private Long id;
    private String symbol;
    private String exchange;
    private String companyName;
    private String sector;
}