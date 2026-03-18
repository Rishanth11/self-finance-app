package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "emi_schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long loanId;
    private int  monthNo;

    private double emiAmount;
    private double principalComponent;
    private double interestComponent;
    private double remainingBalance;

    private LocalDate dueDate;

    @Builder.Default
    private boolean paid = false;

    // ── New field: tracks when the EMI was actually paid ────────
    private LocalDate paidDate;
}