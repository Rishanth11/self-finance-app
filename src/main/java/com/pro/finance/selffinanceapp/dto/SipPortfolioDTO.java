package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SipPortfolioDTO {
    public double totalInvested;
    public double currentValue;
    public double returns;
    public double xirr;
    private boolean navAvailable;
}
