package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.StockWatchlist;
import com.pro.finance.selffinanceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockWatchlistRepository extends JpaRepository<StockWatchlist, Long> {
    List<StockWatchlist> findByUser(User user);
    Optional<StockWatchlist> findByUserAndSymbolAndExchange(User user, String symbol, String exchange);
    boolean existsByUserAndSymbolAndExchange(User user, String symbol, String exchange);
}