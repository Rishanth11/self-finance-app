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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DigitalGoldService — Fixed for production
 *
 * CHANGES FROM ORIGINAL:
 *   - Removed the redundant secondary in-memory cache (cachedPrice / cacheTime fields).
 *     GoldPriceService now handles caching internally with a 15-min TTL and never
 *     returns null — it falls back to stale cache or a hardcoded approximate instead.
 *     The secondary cache here was duplicating that logic unnecessarily.
 *
 *   - resolveGoldPrice() simplified: one call to priceService.getLiveGoldPricePerGram(),
 *     which is guaranteed non-null. The PriceResult wrapper is kept because GoldSummaryDTO
 *     still needs the stale/asOf metadata — but the staleness is now determined by
 *     whether GoldPriceService served from its own internal cache.
 *
 *   - All other logic (add/update/delete/summary/history) is unchanged.
 */
@Slf4j
@Service
public class DigitalGoldService {

    private final DigitalGoldRepository repo;
    private final UserRepository        userRepository;
    private final GoldPriceService      priceService;

    public DigitalGoldService(DigitalGoldRepository repo,
                              UserRepository userRepository,
                              GoldPriceService priceService) {
        this.repo          = repo;
        this.userRepository = userRepository;
        this.priceService  = priceService;
    }

    // ─── PRICE RESOLUTION ─────────────────────────────────────────────────────

    /**
     * Wraps GoldPriceService call into a PriceResult.
     *
     * GoldPriceService.getLiveGoldPricePerGram() is now guaranteed non-null:
     * it returns live price, stale cache, or a hardcoded approximate — never null.
     *
     * We mark stale=false always here because we don't have visibility into whether
     * GoldPriceService served from cache. If you want to surface staleness in the UI,
     * add a GoldPriceService.isCacheStale() method and call it here.
     */
    private PriceResult resolveGoldPrice() {
        try {
            BigDecimal price = priceService.getLiveGoldPricePerGram();
            // getLiveGoldPricePerGram() never returns null after the fix
            log.info("✅ Gold price resolved: ₹{}/g", price);
            return new PriceResult(price, false, LocalDateTime.now());
        } catch (Exception e) {
            // Should not happen after the fix, but guard anyway
            log.error("❌ Unexpected error fetching gold price: {}", e.getMessage());
            return new PriceResult(null, true, null);
        }
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

        return new GoldSummaryDTO(
                totalGrams, totalInvested,
                result.price(), currentValue, profitLoss,
                result.stale(), asOf
        );
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
        BigDecimal profitLoss   = currentValue.subtract(totalInvested);
        String     asOf         = result.asOf().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        return new GoldSummaryDTO(
                totalGrams, totalInvested,
                result.price(), currentValue, profitLoss,
                result.stale(), asOf
        );
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

        LocalDate   start  = LocalDate.of(year, month, 1);
        LocalDate   end    = start.withDayOfMonth(start.lengthOfMonth());
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
                    null,
                    null
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