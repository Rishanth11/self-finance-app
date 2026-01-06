package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sip_investments")
public class SipInvestment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String fundName;

    private String fundCode; // used for NAV API

    private double monthlyAmount;

    private LocalDate startDate;

    private int sipDay; // 1–28 recommended

    private boolean active = true;
}
