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

    @PostMapping("/{sipId}/execute")
    public ResponseEntity<?> runSipNow(@PathVariable Long sipId) {
        sipService.executeSipNow(sipId);
        return ResponseEntity.ok("SIP executed successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getSipByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(sipService.getSipByUser(userId));
    }

    @DeleteMapping("/{sipId}")
    public ResponseEntity<?> deleteSip(@PathVariable Long sipId) {
        sipService.deleteSip(sipId);
        return ResponseEntity.ok("SIP deleted successfully");
    }

    @PutMapping("/{sipId}/deactivate")
    public ResponseEntity<?> deactivateSip(@PathVariable Long sipId) {
        sipService.deactivateSip(sipId);
        return ResponseEntity.ok("SIP deactivated successfully");
    }

    @PutMapping("/{sipId}/activate")
    public ResponseEntity<?> activateSip(@PathVariable Long sipId) {
        sipService.activateSip(sipId);
        return ResponseEntity.ok("SIP activated successfully");
    }


}

