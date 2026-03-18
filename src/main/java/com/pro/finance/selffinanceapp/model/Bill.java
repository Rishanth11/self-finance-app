package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "bill")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String billName;

    @Enumerated(EnumType.STRING)
    private BillCategory category;

    private String notes;

    @Enumerated(EnumType.STRING)
    private BillFrequency frequency;

    private double amount;

    private LocalDate startDate;

    private LocalDate nextDueDate;   // current cycle due date

    @Builder.Default
    private boolean paid = false;    // current cycle paid?

    private LocalDate paidDate;      // when current cycle was paid
}