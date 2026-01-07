package com.pro.finance.selffinanceapp.dto;

import com.pro.finance.selffinanceapp.model.Frequency;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecurringBillDTO {

    private String billName;
    private double amount;
    private Frequency frequency;
    private LocalDate startDate;


}

