package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String fundName;
    private String fundCode;

    @Column(precision = 19, scale = 4)
    private BigDecimal monthlyAmount;

    private LocalDate startDate;
    private int sipDay;
    private boolean active = true;

    @Column(precision = 5, scale = 2)
    private BigDecimal inflationRate = BigDecimal.valueOf(6.0);

    private String goalName;

    @Column(precision = 19, scale = 4)
    private BigDecimal targetAmount;

    private LocalDate targetDate;

}