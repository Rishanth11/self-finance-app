package com.pro.finance.selffinanceapp.controller;
import com.pro.finance.selffinanceapp.model.SipInvestment;
import com.pro.finance.selffinanceapp.service.SipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sip")
public class SipController {

    private final SipService sipService;

    public SipController(SipService sipService) {
        this.sipService = sipService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addSip(@RequestBody SipInvestment sip) {
        return ResponseEntity.ok(sipService.saveSip(sip));
    }

    @GetMapping("/{sipId}/portfolio")
    public ResponseEntity<?> portfolio(@PathVariable Long sipId) {
        return ResponseEntity.ok(sipService.getPortfolio(sipId));
    }

    @GetMapping("/{sipId}/chart")
    public List<Map<String, Object>> sipChart(@PathVariable Long sipId) {
        return sipService.getSipChart(sipId);
    }
}

