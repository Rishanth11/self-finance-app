package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.DigitalGoldDTO;
import com.pro.finance.selffinanceapp.dto.GoldHistoryDTO;
import com.pro.finance.selffinanceapp.dto.GoldSummaryDTO;
import com.pro.finance.selffinanceapp.model.DigitalGold;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.DigitalGoldRepository;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import org.springframework.stereotype.Service;

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
        gold.setTotalInvested(
                dto.getGramsPurchased() * dto.getPurchasePricePerGram()
        );
        gold.setUser(user);

        return repo.save(gold);
    }

    public GoldSummaryDTO getSummary(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<DigitalGold> list = repo.findByUserId(user.getId());

        double totalGrams = 0;
        double totalInvested = 0;

        for (DigitalGold gold : list) {
            totalGrams += gold.getGramsPurchased();
            totalInvested += gold.getTotalInvested();
        }

        double livePrice = priceService.getLiveGoldPricePerGram();
        double currentValue = totalGrams * livePrice;
        double profitLoss = currentValue - totalInvested;

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

        List<DigitalGold> list = repo.findByUserId(user.getId());
        double livePrice = priceService.getLiveGoldPricePerGram();

        return list.stream().map(gold -> {

            double currentValue =
                    gold.getGramsPurchased() * livePrice;

            double profit =
                    currentValue - gold.getTotalInvested();

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
                .orElseThrow(() -> new RuntimeException("Not found"));

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
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!gold.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        gold.setGramsPurchased(dto.getGramsPurchased());
        gold.setPurchasePricePerGram(dto.getPurchasePricePerGram());
        gold.setPurchaseDate(dto.getPurchaseDate());
        gold.setTotalInvested(
                dto.getGramsPurchased() * dto.getPurchasePricePerGram()
        );

        return repo.save(gold);
    }

    public GoldSummaryDTO getFilteredSummary(
            String email,
            int year,
            int month) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<DigitalGold> list =
                repo.findByUserIdAndPurchaseDateBetween(
                        user.getId(), start, end);

        double totalGrams = 0;
        double totalInvested = 0;

        for (DigitalGold gold : list) {
            totalGrams += gold.getGramsPurchased();
            totalInvested += gold.getTotalInvested();
        }

        double livePrice = priceService.getLiveGoldPricePerGram();
        double currentValue = totalGrams * livePrice;
        double profitLoss = currentValue - totalInvested;

        return new GoldSummaryDTO(
                totalGrams,
                totalInvested,
                livePrice,
                currentValue,
                profitLoss
        );
    }

    public List<DigitalGold> getFilteredHistory(
            String email,
            int year,
            int month) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return repo.findByUserIdAndPurchaseDateBetween(
                user.getId(), start, end);
    }
}