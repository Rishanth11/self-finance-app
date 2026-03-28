package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.DigitalGoldDTO;
import com.pro.finance.selffinanceapp.dto.GoldHistoryDTO;
import com.pro.finance.selffinanceapp.dto.GoldSummaryDTO;
import com.pro.finance.selffinanceapp.model.DigitalGold;
import com.pro.finance.selffinanceapp.service.DigitalGoldService;

import com.pro.finance.selffinanceapp.service.GoldPriceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/gold")
public class DigitalGoldController {

    private final DigitalGoldService service;

    private final GoldPriceService goldPriceService;

    public DigitalGoldController(DigitalGoldService service, GoldPriceService goldPriceService) {
        this.service = service;
        this.goldPriceService = goldPriceService;
    }

    @PostMapping
    public DigitalGold addGold(@Valid @RequestBody DigitalGoldDTO dto,
                               Principal principal) {
        return service.addGold(dto, principal.getName());
    }

    @GetMapping("/summary")
    public GoldSummaryDTO getSummary(Principal principal) {
        return service.getSummary(principal.getName());
    }

    @GetMapping
    public List<GoldHistoryDTO> getAllGold(Principal principal) {
        return service.getAllGold(principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteGold(@PathVariable Long id,
                           Principal principal) {
        service.deleteGold(id, principal.getName());
    }

    @PutMapping("/{id}")
    public DigitalGold updateGold(@PathVariable Long id,
                                  @Valid @RequestBody DigitalGoldDTO dto,  // ✅ @Valid added
                                  Principal principal) {
        return service.updateGold(id, dto, principal.getName());
    }

    @GetMapping("/summary/filter")
    public GoldSummaryDTO getFilteredSummary(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {
        return service.getFilteredSummary(principal.getName(), year, month);
    }

    @GetMapping("/filter")
    public List<GoldHistoryDTO> getFilteredHistory(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {
        return service.getFilteredHistory(principal.getName(), year, month);
    }

    @GetMapping("/price")
    public ResponseEntity<BigDecimal> getGoldPrice() {
        BigDecimal price = goldPriceService.getLiveGoldPricePerGram();
        return price != null ? ResponseEntity.ok(price) : ResponseEntity.status(503).build();
    }
}