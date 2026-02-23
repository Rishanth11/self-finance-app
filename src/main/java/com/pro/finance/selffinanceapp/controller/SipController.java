package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.SipRequestDTO;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.SipService;
import com.pro.finance.selffinanceapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sip")
@PreAuthorize("hasRole('USER')")
public class SipController {

    private final SipService sipService;
    private final UserService userService;

    public SipController(SipService sipService, UserService userService) {
        this.sipService = sipService;
        this.userService = userService;
    }

    // ✅ CREATE SIP
    @PostMapping
    public ResponseEntity<?> addSip(@RequestBody SipRequestDTO dto,
                                    Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        return ResponseEntity.ok(
                sipService.createSip(dto, user)
        );
    }

    // ✅ GET ALL SIPS
    @GetMapping
    public ResponseEntity<?> getAll(Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        return ResponseEntity.ok(
                sipService.getAllByUser(user)
        );
    }

    // ✅ PORTFOLIO
    @GetMapping("/{sipId}/portfolio")
    public ResponseEntity<?> portfolio(@PathVariable Long sipId,
                                       Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        return ResponseEntity.ok(
                sipService.getPortfolio(sipId, user)
        );
    }

    // ✅ CHART
    @GetMapping("/{sipId}/chart")
    public ResponseEntity<?> sipChart(@PathVariable Long sipId,
                                      Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        return ResponseEntity.ok(
                sipService.getSipChart(sipId, user)
        );
    }

    // ✅ EXECUTE SIP
    @PostMapping("/{sipId}/execute")
    public ResponseEntity<?> execute(@PathVariable Long sipId,
                                     Authentication authentication) {

        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            sipService.executeSipNow(sipId, user);

            return ResponseEntity.ok("SIP executed successfully");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ DELETE SIP
    @DeleteMapping("/{sipId}")
    public ResponseEntity<?> delete(@PathVariable Long sipId,
                                    Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        sipService.deleteSip(sipId, user);

        return ResponseEntity.ok("SIP deleted successfully");
    }

    // ✅ DEACTIVATE SIP
    @PutMapping("/{sipId}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long sipId,
                                        Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        sipService.deactivateSip(sipId, user);

        return ResponseEntity.ok("SIP deactivated");
    }

    // ✅ ACTIVATE SIP
    @PutMapping("/{sipId}/activate")
    public ResponseEntity<?> activate(@PathVariable Long sipId,
                                      Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email);

        sipService.activateSip(sipId, user);

        return ResponseEntity.ok("SIP activated");
    }
}