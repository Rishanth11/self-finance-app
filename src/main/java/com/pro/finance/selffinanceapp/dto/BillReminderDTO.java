package com.pro.finance.selffinanceapp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BillReminderDTO {

    private int    overdueCount;
    private int    dueTodayCount;
    private int    dueSoonCount;          // due within 7 days (excluding today)

    private double totalOverdueAmount;
    private double totalDueSoonAmount;    // due today + due soon combined

    private List<BillResponseDTO> overdueList;
    private List<BillResponseDTO> dueTodayList;
    private List<BillResponseDTO> dueSoonList;
}