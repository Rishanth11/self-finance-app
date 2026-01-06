package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SipRequestDTO {
    public String fundName;
    public String fundCode;
    public double monthlyAmount;
    public LocalDate startDate;
    public int sipDay;
}
