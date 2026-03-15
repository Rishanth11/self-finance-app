package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.StockHoldingDTO;
import com.pro.finance.selffinanceapp.dto.StockWatchlistDTO;
import com.pro.finance.selffinanceapp.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // ── HOLDINGS ──

    @GetMapping("/holdings")
    public ResponseEntity<?> getHoldings(Principal principal) {
        try {
            return ResponseEntity.ok(stockService.getHoldings(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/holdings")
    public ResponseEntity<?> addHolding(@RequestBody StockHoldingDTO dto, Principal principal) {
        try {
            return ResponseEntity.ok(stockService.addHolding(principal.getName(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/holdings/{id}")
    public ResponseEntity<?> updateHolding(@PathVariable Long id,
                                           @RequestBody StockHoldingDTO dto,
                                           Principal principal) {
        try {
            return ResponseEntity.ok(stockService.updateHolding(id, principal.getName(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/holdings/{id}")
    public ResponseEntity<?> deleteHolding(@PathVariable Long id, Principal principal) {
        try {
            stockService.deleteHolding(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // ── WATCHLIST ──

    @GetMapping("/watchlist")
    public ResponseEntity<?> getWatchlist(Principal principal) {
        try {
            return ResponseEntity.ok(stockService.getWatchlist(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/watchlist")
    public ResponseEntity<?> addToWatchlist(@RequestBody StockWatchlistDTO dto,
                                            Principal principal) {
        try {
            return ResponseEntity.ok(stockService.addToWatchlist(principal.getName(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/watchlist/{id}")
    public ResponseEntity<?> removeFromWatchlist(@PathVariable Long id, Principal principal) {
        try {
            stockService.removeFromWatchlist(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}