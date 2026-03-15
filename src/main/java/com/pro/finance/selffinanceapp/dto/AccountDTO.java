package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal openingBalance;
    private BigDecimal balance;
}