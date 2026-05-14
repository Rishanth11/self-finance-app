package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.StockHolding;
import com.rishanth.flux360.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findByUser(User user);
    boolean existsByUserAndSymbolAndExchange(User user, String symbol, String exchange);
}