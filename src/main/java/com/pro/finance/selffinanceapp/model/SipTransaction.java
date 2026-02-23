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
@Table(name = "sip_transactions")
public class SipTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sip_id", nullable = false)
    private SipInvestment sip;

    private LocalDate investDate;

    @Column(precision = 19, scale = 6)
    private BigDecimal nav;

    @Column(precision = 19, scale = 6)
    private BigDecimal units;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;
}