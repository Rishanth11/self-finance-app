package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.DigitalGoldDTO;
import com.pro.finance.selffinanceapp.model.DigitalGold;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.DigitalGoldRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DigitalGoldService {

    private final DigitalGoldRepository repo;
    private final GoldPriceService priceService;

    public DigitalGoldService(DigitalGoldRepository repo,
                              GoldPriceService priceService) {
        this.repo = repo;
        this.priceService = priceService;
    }

    // ➕ Add Gold
    public DigitalGold addGold(DigitalGoldDTO dto, User user) {

        double livePrice = priceService.getLiveGoldPricePerGram();

        DigitalGold gold = new DigitalGold();
        gold.setGramsPurchased(dto.getGramsPurchased());
        gold.setPurchasePricePerGram(dto.getPurchasePricePerGram());

        double invested = dto.getGramsPurchased() * dto.getPurchasePricePerGram();
        gold.setTotalInvested(invested);

        gold.setCurrentPricePerGram(livePrice);
        gold.setCurrentValue(dto.getGramsPurchased() * livePrice);
        gold.setProfitLoss(gold.getCurrentValue() - invested);

        gold.setPurchaseDate(dto.getPurchaseDate());
        gold.setUser(user);

        return repo.save(gold);
    }

    // 🔄 Update Live Value
    public List<DigitalGold> updateGoldValue(Long userId) {

        double livePrice = priceService.getLiveGoldPricePerGram();
        List<DigitalGold> goldList = repo.findByUserId(userId);

        goldList.forEach(gold -> {
            gold.setCurrentPricePerGram(livePrice);
            gold.setCurrentValue(gold.getGramsPurchased() * livePrice);
            gold.setProfitLoss(
                    gold.getCurrentValue() - gold.getTotalInvested()
            );
        });

        return repo.saveAll(goldList);
    }
}

