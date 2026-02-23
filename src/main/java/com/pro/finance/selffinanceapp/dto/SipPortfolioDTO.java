package com.pro.finance.selffinanceapp.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SipPortfolioDTO {

    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal returns;
    private BigDecimal xirr;
    private BigDecimal realReturn;
    private BigDecimal goalProgress;
    private boolean navAvailable;
    private String goalName;
    private BigDecimal targetAmount;
}