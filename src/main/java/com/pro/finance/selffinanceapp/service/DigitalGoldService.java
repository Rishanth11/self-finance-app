package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.DigitalGoldDTO;
import com.pro.finance.selffinanceapp.dto.GoldHistoryDTO;
import com.pro.finance.selffinanceapp.dto.GoldSummaryDTO;
import com.pro.finance.selffinanceapp.model.DigitalGold;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.DigitalGoldRepository;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DigitalGoldService {

    private final DigitalGoldRepository repo;
    private final UserRepository userRepository;
    private final GoldPriceService priceService;

    public DigitalGoldService(DigitalGoldRepository repo,
                              UserRepository userRepository,
                              GoldPriceService priceService) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.priceService = priceService;
    }

    public DigitalGold addGold(DigitalGoldDTO dto, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalGold gold = new DigitalGold();

        gold.setGramsPurchased(dto.getGramsPurchased());
        gold.setPurchasePricePerGram(dto.getPurchasePricePerGram());
        gold.setPurchaseDate(dto.getPurchaseDate());

        BigDecimal totalInvested =
                dto.getGramsPurchased().multiply(dto.getPurchasePricePerGram());

        gold.setTotalInvested(totalInvested);
        gold.setUser(user);

        return repo.save(gold);
    }

    // 🔥 COMMON METHOD (avoid repeating logic)
    private BigDecimal getSafeGoldPrice() {
        BigDecimal price = priceService.getLiveGoldPricePerGram();

        if (price == null) {
            System.out.println("❌ Gold API failed - price unavailable");
            throw new RuntimeException("Gold price unavailable. Please try again later.");
        }

        System.out.println("✅ Gold price fetched: " + price);
        return price;
    }

    public GoldSummaryDTO getSummary(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DigitalGold> list =
                repo.findByUserIdOrderByPurchaseDateDesc(user.getId());

        BigDecimal totalGrams = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;

        for (DigitalGold gold : list) {
            totalGrams = totalGrams.add(gold.getGramsPurchased());
            totalInvested = totalInvested.add(gold.getTotalInvested());
        }

        BigDecimal livePrice = getSafeGoldPrice();

        BigDecimal currentValue = totalGrams.multiply(livePrice);
        BigDecimal profitLoss = currentValue.subtract(totalInvested);

        return new GoldSummaryDTO(
                totalGrams,
                totalInvested,
                livePrice,
                currentValue,
                profitLoss
        );
    }

    public List<GoldHistoryDTO> getAllGold(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DigitalGold> list =
                repo.findByUserIdOrderByPurchaseDateDesc(user.getId());

        BigDecimal livePrice = getSafeGoldPrice();

        return list.stream().map(gold -> {

            BigDecimal currentValue =
                    gold.getGramsPurchased().multiply(livePrice);

            BigDecimal profit =
                    currentValue.subtract(gold.getTotalInvested());

            return new GoldHistoryDTO(
                    gold.getId(),
                    gold.getPurchaseDate().toString(),
                    gold.getGramsPurchased(),
                    gold.getPurchasePricePerGram(),
                    gold.getTotalInvested(),
                    currentValue,
                    profit
            );

        }).toList();
    }

    public void deleteGold(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalGold gold = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gold record not found"));

        if (!gold.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        repo.delete(gold);
    }

    public DigitalGold updateGold(Long id,
                                  DigitalGoldDTO dto,
                                  String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DigitalGold gold = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gold record not found"));

        if (!gold.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        gold.setGramsPurchased(dto.getGramsPurchased());
        gold.setPurchasePricePerGram(dto.getPurchasePricePerGram());
        gold.setPurchaseDate(dto.getPurchaseDate());

        BigDecimal totalInvested =
                dto.getGramsPurchased().multiply(dto.getPurchasePricePerGram());

        gold.setTotalInvested(totalInvested);

        return repo.save(gold);
    }

    public GoldSummaryDTO getFilteredSummary(String email, int year, int month) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<DigitalGold> list =
                repo.findByUserIdAndPurchaseDateBetween(
                        user.getId(), start, end);

        BigDecimal totalGrams = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;

        for (DigitalGold gold : list) {
            totalGrams = totalGrams.add(gold.getGramsPurchased());
            totalInvested = totalInvested.add(gold.getTotalInvested());
        }

        BigDecimal livePrice = getSafeGoldPrice();

        BigDecimal currentValue = totalGrams.multiply(livePrice);
        BigDecimal profitLoss = currentValue.subtract(totalInvested);

        return new GoldSummaryDTO(
                totalGrams,
                totalInvested,
                livePrice,
                currentValue,
                profitLoss
        );
    }

    public List<GoldHistoryDTO> getFilteredHistory(String email, int year, int month) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        BigDecimal livePrice = getSafeGoldPrice();

        return repo.findByUserIdAndPurchaseDateBetween(user.getId(), start, end)
                .stream()
                .map(gold -> {

                    BigDecimal currentValue =
                            gold.getGramsPurchased().multiply(livePrice);

                    BigDecimal profit =
                            currentValue.subtract(gold.getTotalInvested());

                    return new GoldHistoryDTO(
                            gold.getId(),
                            gold.getPurchaseDate().toString(),
                            gold.getGramsPurchased(),
                            gold.getPurchasePricePerGram(),
                            gold.getTotalInvested(),
                            currentValue,
                            profit
                    );
                }).toList();
    }
}