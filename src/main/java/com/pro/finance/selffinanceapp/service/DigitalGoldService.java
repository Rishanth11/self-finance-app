package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.DigitalGoldDTO;
import com.pro.finance.selffinanceapp.dto.GoldHistoryDTO;
import com.pro.finance.selffinanceapp.dto.GoldSummaryDTO;
import com.pro.finance.selffinanceapp.model.DigitalGold;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.DigitalGoldRepository;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class DigitalGoldService {

    private final DigitalGoldRepository repo;
    private final UserRepository userRepository;
    private final GoldPriceService priceService;

    // ─── In-memory fallback cache (used only when live fetch fails) ───────────
    // NOTE: CacheConfig / Caffeine handles the primary cache on GoldPriceService.
    //       This secondary cache is the last-resort fallback so getSummary() never
    //       throws even when both the live API and Caffeine are cold.
    private BigDecimal cachedPrice = null;
    private LocalDateTime cacheTime = null;
    private static final int CACHE_TTL_MINUTES = 30;

    public DigitalGoldService(DigitalGoldRepository repo,
                              UserRepository userRepository,
                              GoldPriceService priceService) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.priceService = priceService;
    }

    // ─── PRICE RESOLUTION (cache + fallback, never throws) ────────────────────

    /**
     * Returns a PriceResult with the gold price and whether it's stale.
     * Never throws — callers always get a result or a clear null signal.
     */
    private PriceResult resolveGoldPrice() {
        // 1. Try live price (Caffeine-cached inside GoldPriceService)
        try {
            BigDecimal live = priceService.getLiveGoldPricePerGram();
            if (live != null) {
                cachedPrice = live;
                cacheTime   = LocalDateTime.now();
                log.info("✅ Gold price fetched: {}", live);
                return new PriceResult(live, false, cacheTime);
            }
        } catch (Exception e) {
            log.warn("⚠️ Live gold price fetch failed: {}", e.getMessage());
        }

        // 2. Fall back to in-memory cache only if still within TTL
        if (cachedPrice != null && cacheTime != null) {
            long minutesOld = Duration.between(cacheTime, LocalDateTime.now()).toMinutes();
            if (minutesOld < CACHE_TTL_MINUTES) {
                log.warn("⚠️ Using cached gold price ({} min old): {}", minutesOld, cachedPrice);
                return new PriceResult(cachedPrice, true, cacheTime);
            }
            log.warn("⚠️ Cached gold price is {} min old (> {} min TTL) — treating as stale", minutesOld, CACHE_TTL_MINUTES);
        }

        // 3. No live price, no usable cache
        log.error("❌ Gold price completely unavailable — no live data and no valid cache");
        return new PriceResult(null, true, null);
    }

    /** Simple value holder for price + staleness metadata */
    private record PriceResult(BigDecimal price, boolean stale, LocalDateTime asOf) {
        boolean isAvailable() { return price != null; }
    }

    // ─── ADD / UPDATE / DELETE ────────────────────────────────────────────────

    public DigitalGold addGold(DigitalGoldDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalGold gold = new DigitalGold();
        gold.setGramsPurchased(dto.getGramsPurchased());
        gold.setPurchasePricePerGram(dto.getPurchasePricePerGram());
        gold.setPurchaseDate(dto.getPurchaseDate());
        gold.setTotalInvested(dto.getGramsPurchased().multiply(dto.getPurchasePricePerGram()));
        gold.setUser(user);

        return repo.save(gold);
    }

    public DigitalGold updateGold(Long id, DigitalGoldDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalGold gold = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gold record not found"));

        if (!gold.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied");

        gold.setGramsPurchased(dto.getGramsPurchased());
        gold.setPurchasePricePerGram(dto.getPurchasePricePerGram());
        gold.setPurchaseDate(dto.getPurchaseDate());
        gold.setTotalInvested(dto.getGramsPurchased().multiply(dto.getPurchasePricePerGram()));

        return repo.save(gold);
    }

    public void deleteGold(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalGold gold = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gold record not found"));

        if (!gold.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Access denied");

        repo.delete(gold);
    }

    // ─── SUMMARY ──────────────────────────────────────────────────────────────

    public GoldSummaryDTO getSummary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DigitalGold> list = repo.findByUserIdOrderByPurchaseDateDesc(user.getId());

        BigDecimal totalGrams    = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;

        for (DigitalGold gold : list) {
            totalGrams    = totalGrams.add(gold.getGramsPurchased());
            totalInvested = totalInvested.add(gold.getTotalInvested());
        }

        PriceResult result = resolveGoldPrice();

        if (!result.isAvailable()) {
            return new GoldSummaryDTO(totalGrams, totalInvested, null, null, null, true, null);
        }

        BigDecimal currentValue = totalGrams.multiply(result.price());
        BigDecimal profitLoss   = currentValue.subtract(totalInvested);
        String     asOf         = result.asOf().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        return new GoldSummaryDTO(totalGrams, totalInvested, result.price(), currentValue, profitLoss, result.stale(), asOf);
    }

    public GoldSummaryDTO getFilteredSummary(String email, int year, int month) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        List<DigitalGold> list = repo.findByUserIdAndPurchaseDateBetween(user.getId(), start, end);

        BigDecimal totalGrams    = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;

        for (DigitalGold gold : list) {
            totalGrams    = totalGrams.add(gold.getGramsPurchased());
            totalInvested = totalInvested.add(gold.getTotalInvested());
        }

        PriceResult result = resolveGoldPrice();

        if (!result.isAvailable()) {
            return new GoldSummaryDTO(totalGrams, totalInvested, null, null, null, true, null);
        }

        BigDecimal currentValue = totalGrams.multiply(result.price());
        BigDecimal profitLoss   = currentValue.subtract(totalInvested);   // named variable — symmetric with getSummary()
        String     asOf         = result.asOf().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        return new GoldSummaryDTO(totalGrams, totalInvested, result.price(), currentValue, profitLoss, result.stale(), asOf);
    }

    // ─── HISTORY ──────────────────────────────────────────────────────────────

    public List<GoldHistoryDTO> getAllGold(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DigitalGold> list = repo.findByUserIdOrderByPurchaseDateDesc(user.getId());
        PriceResult result     = resolveGoldPrice();

        return list.stream().map(gold -> toHistoryDTO(gold, result)).toList();
    }

    public List<GoldHistoryDTO> getFilteredHistory(String email, int year, int month) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start    = LocalDate.of(year, month, 1);
        LocalDate end      = start.withDayOfMonth(start.lengthOfMonth());
        PriceResult result = resolveGoldPrice();

        return repo.findByUserIdAndPurchaseDateBetween(user.getId(), start, end)
                .stream()
                .map(gold -> toHistoryDTO(gold, result))
                .toList();
    }

    private GoldHistoryDTO toHistoryDTO(DigitalGold gold, PriceResult result) {
        if (!result.isAvailable()) {
            return new GoldHistoryDTO(
                    gold.getId(),
                    gold.getPurchaseDate().toString(),
                    gold.getGramsPurchased(),
                    gold.getPurchasePricePerGram(),
                    gold.getTotalInvested(),
                    null,   // currentValue unavailable
                    null    // profit unavailable
            );
        }

        BigDecimal currentValue = gold.getGramsPurchased().multiply(result.price());
        BigDecimal profit       = currentValue.subtract(gold.getTotalInvested());

        return new GoldHistoryDTO(
                gold.getId(),
                gold.getPurchaseDate().toString(),
                gold.getGramsPurchased(),
                gold.getPurchasePricePerGram(),
                gold.getTotalInvested(),
                currentValue,
                profit
        );
    }
}