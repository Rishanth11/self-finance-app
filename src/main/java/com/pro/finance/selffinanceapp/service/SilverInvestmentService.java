package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.SilverInvestmentPnLDTO;
import com.pro.finance.selffinanceapp.dto.SilverPortfolioSummaryDTO;
import com.pro.finance.selffinanceapp.model.SilverInvestment;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.SilverInvestmentRepository;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SilverInvestmentService {

    private final SilverInvestmentRepository silverInvestmentRepository;
    private final SilverPriceService silverPriceService;
    private final UserRepository userRepository;

    public SilverInvestment addInvestment(String username, BigDecimal grams,
                                          BigDecimal pricePerGram, LocalDate purchaseDate) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        SilverInvestment investment = new SilverInvestment();
        investment.setUser(user);
        investment.setGrams(grams);
        investment.setPricePerGram(pricePerGram);
        investment.setPurchaseDate(purchaseDate);
        return silverInvestmentRepository.save(investment);
    }

    public SilverPortfolioSummaryDTO getPortfolioSummary(String username) {

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<SilverInvestment> records = silverInvestmentRepository.findByUser(user);

        if (records.isEmpty()) {
            return new SilverPortfolioSummaryDTO(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    Collections.emptyList()
            );
        }

        // ── FIX: getLiveSilverPricePerGram() now NEVER returns null ───────────
        // It returns stale cache or hardcoded approximate — so we never throw 500.
        // Previously: if(price == null) throw RuntimeException → HTTP 500
        BigDecimal currentPrice = silverPriceService.getLiveSilverPricePerGram();

        System.out.println("✅ Silver price for portfolio calculation: ₹" + currentPrice + "/g");

        List<SilverInvestmentPnLDTO> investments = records.stream()
                .map(inv -> mapToPnLDTO(inv, currentPrice))
                .collect(Collectors.toList());

        BigDecimal totalInvested = investments.stream()
                .map(SilverInvestmentPnLDTO::getInvestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrentValue = investments.stream()
                .map(SilverInvestmentPnLDTO::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGrams = investments.stream()
                .map(SilverInvestmentPnLDTO::getGrams)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPnL = totalCurrentValue.subtract(totalInvested);

        BigDecimal totalPnLPercent = totalInvested.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalPnL.divide(totalInvested, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        return new SilverPortfolioSummaryDTO(
                totalGrams,
                totalInvested.setScale(2, RoundingMode.HALF_UP),
                totalCurrentValue.setScale(2, RoundingMode.HALF_UP),
                totalPnL.setScale(2, RoundingMode.HALF_UP),
                totalPnLPercent,
                investments
        );
    }

    public void deleteInvestment(Long investmentId, String username) {
        SilverInvestment inv = silverInvestmentRepository.findById(investmentId)
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        if (!inv.getUser().getEmail().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        silverInvestmentRepository.delete(inv);
    }

    public SilverInvestment updateInvestment(Long id, SilverInvestment body, String username) {
        SilverInvestment inv = silverInvestmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        if (!inv.getUser().getEmail().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        inv.setGrams(body.getGrams());
        inv.setPricePerGram(body.getPricePerGram());
        inv.setPurchaseDate(body.getPurchaseDate());

        return silverInvestmentRepository.save(inv);
    }

    private SilverInvestmentPnLDTO mapToPnLDTO(SilverInvestment inv, BigDecimal currentPrice) {

        BigDecimal invested = inv.getGrams()
                .multiply(inv.getPricePerGram())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentValue = inv.getGrams()
                .multiply(currentPrice)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal pnl = currentValue.subtract(invested);

        BigDecimal pnlPercent = invested.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : pnl.divide(invested, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        return new SilverInvestmentPnLDTO(
                inv.getId(),
                inv.getGrams(),
                inv.getPricePerGram(),
                inv.getPurchaseDate(),
                invested,
                currentPrice,
                currentValue,
                pnl,
                pnlPercent
        );
    }
}