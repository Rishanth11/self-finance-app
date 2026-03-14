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

        BigDecimal livePrice = priceService.getLiveGoldPricePerGram();

        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal profitLoss = BigDecimal.ZERO;

        if (livePrice != null) {
            currentValue = totalGrams.multiply(livePrice);
            profitLoss = currentValue.subtract(totalInvested);
        }

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

        BigDecimal livePrice = priceService.getLiveGoldPricePerGram();

        return list.stream().map(gold -> {

            BigDecimal currentValue = BigDecimal.ZERO;
            BigDecimal profit = BigDecimal.ZERO;

            if (livePrice != null) {
                currentValue =
                        gold.getGramsPurchased().multiply(livePrice);

                profit =
                        currentValue.subtract(gold.getTotalInvested());
            }

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

        BigDecimal livePrice = priceService.getLiveGoldPricePerGram();

        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal profitLoss = BigDecimal.ZERO;

        if (livePrice != null) {
            currentValue = totalGrams.multiply(livePrice);
            profitLoss = currentValue.subtract(totalInvested);
        }

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

        BigDecimal livePrice = priceService.getLiveGoldPricePerGram();

        return repo.findByUserIdAndPurchaseDateBetween(user.getId(), start, end)
                .stream()
                .map(gold -> {

                    BigDecimal currentValue = BigDecimal.ZERO;
                    BigDecimal profit = BigDecimal.ZERO;

                    if (livePrice != null) {
                        currentValue =
                                gold.getGramsPurchased().multiply(livePrice);

                        profit =
                                currentValue.subtract(gold.getTotalInvested());
                    }

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