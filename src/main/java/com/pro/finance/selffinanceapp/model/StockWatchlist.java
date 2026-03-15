package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stock_watchlist")
public class StockWatchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String exchange;     // NSE or BSE
    private String companyName;
    private String sector;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}