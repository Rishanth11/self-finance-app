package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SipRequestDTO {
    public String fundName;
    public String fundCode;
    public BigDecimal monthlyAmount;
    public LocalDate startDate;
    public int sipDay;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal inflationRate;
}
