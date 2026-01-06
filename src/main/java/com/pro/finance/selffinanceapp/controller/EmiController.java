package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.model.EmiSchedule;
import com.pro.finance.selffinanceapp.service.EmiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emi")
public class EmiController {

    private final EmiService emiService;

    public EmiController(EmiService emiService) {
        this.emiService = emiService;
    }

    // 📄 EMI SCHEDULE
    @GetMapping("/loan/{loanId}")
    public List<EmiSchedule> getSchedule(@PathVariable Long loanId) {
        return emiService.getSchedule(loanId);
    }

    // 💸 PAY EMI
    @PutMapping("/pay/{emiId}")
    public String payEmi(@PathVariable Long emiId) {
        emiService.payEmi(emiId);
        return "EMI Paid Successfully";
    }
}
