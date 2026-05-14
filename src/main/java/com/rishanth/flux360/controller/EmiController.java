package com.rishanth.flux360.controller;

import com.rishanth.flux360.dto.EmiScheduleDTO;
import com.rishanth.flux360.service.EmiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emi")
@RequiredArgsConstructor
public class EmiController {

    private final EmiService emiService;

    // 📄 FULL EMI SCHEDULE
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<EmiScheduleDTO>> getSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.getSchedule(loanId));
    }

    // 📅 UPCOMING (unpaid) EMIs only
    @GetMapping("/upcoming/{loanId}")
    public ResponseEntity<List<EmiScheduleDTO>> getUpcomingEmis(@PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.getUpcomingEmis(loanId));
    }

    // 💸 PAY EMI (sequential: must pay month 1 before month 2, etc.)
    @PutMapping("/pay/{emiId}")
    public ResponseEntity<EmiScheduleDTO> payEmi(@PathVariable Long emiId) {
        EmiScheduleDTO result = emiService.payEmi(emiId);
        return ResponseEntity.ok(result);
    }
}