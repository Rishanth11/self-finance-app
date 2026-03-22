package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.SilverPortfolioSummaryDTO;
import com.pro.finance.selffinanceapp.model.SilverInvestment;
import com.pro.finance.selffinanceapp.service.SilverInvestmentService;
import com.pro.finance.selffinanceapp.service.SilverPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/silver")
@RequiredArgsConstructor
public class SilverController {

    private final SilverPriceService silverPriceService;
    private final SilverInvestmentService silverInvestmentService;

    @GetMapping("/price")
    public ResponseEntity<BigDecimal> getSilverPrice() {
        BigDecimal price = silverPriceService.getLiveSilverPricePerGram();
        return price != null ? ResponseEntity.ok(price) : ResponseEntity.status(503).build();
    }

    @PostMapping("/invest")
    public ResponseEntity<?> addInvestment(
            @RequestParam BigDecimal grams,
            @RequestParam BigDecimal pricePerGram,
            @RequestParam(required = false) String purchaseDate,
            Principal principal) {
        try {
            LocalDate date = (purchaseDate != null && !purchaseDate.isEmpty())
                    ? LocalDate.parse(purchaseDate)
                    : LocalDate.now();
            SilverInvestment saved = silverInvestmentService.addInvestment(
                    principal.getName(), grams, pricePerGram, date);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to add investment: " + e.getMessage());
        }
    }

    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(Principal principal) {
        try {
            SilverPortfolioSummaryDTO summary = silverInvestmentService
                    .getPortfolioSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to load portfolio: " + e.getMessage());
        }
    }

    @DeleteMapping("/invest/{id}")
    public ResponseEntity<?> deleteInvestment(@PathVariable Long id, Principal principal) {
        try {
            silverInvestmentService.deleteInvestment(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete investment: " + e.getMessage());
        }
    }

    @PutMapping("/invest/{id}")
    public ResponseEntity<?> updateInvestment(
            @PathVariable Long id,
            @Valid @RequestBody SilverInvestment body,  // ✅ @Valid added
            Principal principal) {
        try {
            SilverInvestment updated = silverInvestmentService.updateInvestment(
                    id, body, principal.getName());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to update investment: " + e.getMessage());
        }
    }
}