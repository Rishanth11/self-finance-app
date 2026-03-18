package com.pro.finance.selffinanceapp.dto;

import com.pro.finance.selffinanceapp.model.BillCategory;
import com.pro.finance.selffinanceapp.model.BillFrequency;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BillResponseDTO {

    private Long          id;
    private Long          userId;
    private String        billName;
    private BillCategory  category;
    private String        categoryLabel;
    private String        notes;
    private BillFrequency frequency;
    private String        frequencyLabel;
    private double        amount;
    private LocalDate     startDate;
    private LocalDate     nextDueDate;
    private boolean       paid;
    private LocalDate     paidDate;
    private String        status;       // PAID, OVERDUE, DUE_TODAY, DUE_SOON, UPCOMING
    private long          daysUntilDue;
}