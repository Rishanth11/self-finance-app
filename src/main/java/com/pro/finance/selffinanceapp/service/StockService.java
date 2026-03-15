package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.StockHoldingDTO;
import com.pro.finance.selffinanceapp.dto.StockWatchlistDTO;
import com.pro.finance.selffinanceapp.model.StockHolding;
import com.pro.finance.selffinanceapp.model.StockWatchlist;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.StockHoldingRepository;
import com.pro.finance.selffinanceapp.repository.StockWatchlistRepository;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockHoldingRepository holdingRepository;
    private final StockWatchlistRepository watchlistRepository;
    private final UserRepository userRepository;

    // ── HOLDINGS ──

    public List<StockHoldingDTO> getHoldings(String username) {
        User user = getUser(username);
        return holdingRepository.findByUser(user)
                .stream().map(this::toHoldingDTO).collect(Collectors.toList());
    }

    public StockHoldingDTO addHolding(String username, StockHoldingDTO dto) {
        User user = getUser(username);
        StockHolding h = new StockHolding();
        h.setUser(user);
        h.setSymbol(dto.getSymbol().toUpperCase().trim());
        h.setExchange(dto.getExchange().toUpperCase().trim());
        h.setCompanyName(dto.getCompanyName());
        h.setSector(dto.getSector());
        h.setQuantity(dto.getQuantity());
        h.setAvgBuyPrice(dto.getAvgBuyPrice());
        h.setBuyDate(dto.getBuyDate());
        return toHoldingDTO(holdingRepository.save(h));
    }

    public StockHoldingDTO updateHolding(Long id, String username, StockHoldingDTO dto) {
        StockHolding h = getHoldingForUser(id, username);
        h.setSymbol(dto.getSymbol().toUpperCase().trim());
        h.setExchange(dto.getExchange().toUpperCase().trim());
        h.setCompanyName(dto.getCompanyName());
        h.setSector(dto.getSector());
        h.setQuantity(dto.getQuantity());
        h.setAvgBuyPrice(dto.getAvgBuyPrice());
        h.setBuyDate(dto.getBuyDate());
        return toHoldingDTO(holdingRepository.save(h));
    }

    public void deleteHolding(Long id, String username) {
        holdingRepository.delete(getHoldingForUser(id, username));
    }

    // ── WATCHLIST ──

    public List<StockWatchlistDTO> getWatchlist(String username) {
        User user = getUser(username);
        return watchlistRepository.findByUser(user)
                .stream().map(this::toWatchlistDTO).collect(Collectors.toList());
    }

    public StockWatchlistDTO addToWatchlist(String username, StockWatchlistDTO dto) {
        User user = getUser(username);
        if (watchlistRepository.existsByUserAndSymbolAndExchange(
                user, dto.getSymbol().toUpperCase(), dto.getExchange().toUpperCase())) {
            throw new RuntimeException("Stock already in watchlist");
        }
        StockWatchlist w = new StockWatchlist();
        w.setUser(user);
        w.setSymbol(dto.getSymbol().toUpperCase().trim());
        w.setExchange(dto.getExchange().toUpperCase().trim());
        w.setCompanyName(dto.getCompanyName());
        w.setSector(dto.getSector());
        return toWatchlistDTO(watchlistRepository.save(w));
    }

    public void removeFromWatchlist(Long id, String username) {
        User user = getUser(username);
        StockWatchlist w = watchlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        if (!w.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        watchlistRepository.delete(w);
    }

    // ── HELPERS ──

    private StockHolding getHoldingForUser(Long id, String username) {
        User user = getUser(username);
        StockHolding h = holdingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holding not found"));
        if (!h.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Unauthorized");
        return h;
    }

    private User getUser(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private StockHoldingDTO toHoldingDTO(StockHolding h) {
        return new StockHoldingDTO(h.getId(), h.getSymbol(), h.getExchange(),
                h.getCompanyName(), h.getSector(), h.getQuantity(),
                h.getAvgBuyPrice(), h.getBuyDate());
    }

    private StockWatchlistDTO toWatchlistDTO(StockWatchlist w) {
        return new StockWatchlistDTO(w.getId(), w.getSymbol(),
                w.getExchange(), w.getCompanyName(), w.getSector());
    }
}