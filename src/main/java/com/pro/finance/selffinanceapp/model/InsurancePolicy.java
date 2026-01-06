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
@Table(name = "insurance_policy")
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String userEmail;


    private Long userId;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType; // LIC, TERM, HEALTH, VEHICLE

    private String policyNumber;
    private String providerName;

    private double premiumAmount;

    @Enumerated(EnumType.STRING)
    private PremiumFrequency frequency; // MONTHLY, QUARTERLY, YEARLY

    private LocalDate startDate;
    private LocalDate maturityDate;
    private LocalDate nextPremiumDate;

    private boolean active;

    // path or URL of uploaded document
    private String documentPath;

    // getters & setters
}

