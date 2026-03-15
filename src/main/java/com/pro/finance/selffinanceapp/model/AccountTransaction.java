package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "account_transactions")
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;  // CREDIT, DEBIT, TRANSFER_IN, TRANSFER_OUT

    private String description;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // For transfers — links the two sides together
    private Long transferPairId;

    public enum TransactionType {
        CREDIT, DEBIT, TRANSFER_IN, TRANSFER_OUT
    }
}