package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.StockWatchlist;
import com.rishanth.flux360.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockWatchlistRepository extends JpaRepository<StockWatchlist, Long> {
    List<StockWatchlist> findByUser(User user);
    Optional<StockWatchlist> findByUserAndSymbolAndExchange(User user, String symbol, String exchange);
    boolean existsByUserAndSymbolAndExchange(User user, String symbol, String exchange);
}