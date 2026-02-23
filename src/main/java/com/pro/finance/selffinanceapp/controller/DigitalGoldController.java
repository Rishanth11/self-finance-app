package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.DigitalGoldDTO;
import com.pro.finance.selffinanceapp.dto.GoldHistoryDTO;
import com.pro.finance.selffinanceapp.dto.GoldSummaryDTO;
import com.pro.finance.selffinanceapp.model.DigitalGold;
import com.pro.finance.selffinanceapp.service.DigitalGoldService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/gold")
public class DigitalGoldController {

    private final DigitalGoldService service;

    public DigitalGoldController(DigitalGoldService service) {
        this.service = service;
    }

    // ➕ Add Gold
    @PostMapping
    public DigitalGold addGold(@RequestBody DigitalGoldDTO dto,
                               Principal principal) {
        return service.addGold(dto, principal.getName());
    }

    // 📊 Summary
    @GetMapping("/summary")
    public GoldSummaryDTO getSummary(Principal principal) {
        return service.getSummary(principal.getName());
    }

    // 📋 Purchase History
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
                                  @RequestBody DigitalGoldDTO dto,
                                  Principal principal) {
        return service.updateGold(id, dto, principal.getName());
    }

    @GetMapping("/summary/filter")
    public GoldSummaryDTO getFilteredSummary(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {

        return service.getFilteredSummary(
                principal.getName(), year, month);
    }

    @GetMapping("/filter")
    public List<DigitalGold> getFilteredHistory(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {

        return service.getFilteredHistory(
                principal.getName(), year, month);
    }
}