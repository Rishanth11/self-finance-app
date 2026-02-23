package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DigitalGold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double gramsPurchased;
    private double purchasePricePerGram;
    private double totalInvested;

    private LocalDate purchaseDate;

    @ManyToOne
    private User user;
}

