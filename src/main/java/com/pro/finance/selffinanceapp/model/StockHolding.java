package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "stock_holdings")
public class StockHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;       // e.g. RELIANCE, TCS
    private String exchange;     // NSE or BSE
    private String companyName;
    private String sector;       // IT, Banking, Pharma, etc.

    private Integer quantity;
    private BigDecimal avgBuyPrice;
    private LocalDate buyDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}