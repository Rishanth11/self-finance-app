package com.pro.finance.selffinanceapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

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

    private String fundCode;

    private double monthlyAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    private int sipDay;

    private boolean active = true;
}
