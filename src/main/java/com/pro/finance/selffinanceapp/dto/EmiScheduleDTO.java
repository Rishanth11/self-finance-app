package com.pro.finance.selffinanceapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EmiScheduleDTO {

    private Long      id;
    private Long      loanId;
    private int       monthNo;

    private double    emiAmount;
    private double    principalComponent;
    private double    interestComponent;
    private double    remainingBalance;

    private LocalDate dueDate;
    private boolean   paid;
    private LocalDate paidDate;

    // Convenience field for UI display
    private String    status;   // "PAID", "OVERDUE", "UPCOMING", "DUE_TODAY"
}