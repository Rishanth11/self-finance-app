package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.StockHolding;
import com.pro.finance.selffinanceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findByUser(User user);
    boolean existsByUserAndSymbolAndExchange(User user, String symbol, String exchange);
}