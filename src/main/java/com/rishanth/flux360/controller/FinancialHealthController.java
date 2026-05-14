package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.FinancialHealthDTO;
import com.rishanth.flux360.service.FinancialHealthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial-health")
public class FinancialHealthController {

    private final FinancialHealthService financialHealthService;

    public FinancialHealthController(FinancialHealthService financialHealthService) {
        this.financialHealthService = financialHealthService;
    }

    @GetMapping("/financial-health/{userId}")
    public FinancialHealthDTO getHealthScore(@PathVariable Long userId) {
        return financialHealthService.calculateHealthScore(userId);
    }
}
