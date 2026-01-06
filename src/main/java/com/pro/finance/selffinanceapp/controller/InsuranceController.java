package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.model.InsurancePolicy;
import com.pro.finance.selffinanceapp.service.InsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

    @Autowired
    private InsuranceService service;

    // ADD POLICY
    @PostMapping
    public InsurancePolicy addPolicy(
            @RequestBody InsurancePolicy policy,
            @AuthenticationPrincipal UserDetails user
    ) {
        // username = email
        policy.setUserEmail(user.getUsername());
        return service.addPolicy(policy);
    }

    // GET MY POLICIES
    @GetMapping
    public List<InsurancePolicy> getMyPolicies(
            @AuthenticationPrincipal UserDetails user
    ) {
        return service.getUserPolicies(user.getUsername());
    }

    // PREMIUM REMINDERS
    @GetMapping("/reminders")
    public List<InsurancePolicy> premiumReminders() {
        return service.getPremiumReminders();
    }

    // MATURITY TRACKING
    @GetMapping("/maturity")
    public List<InsurancePolicy> maturityTracking() {
        return service.getMaturityPolicies();
    }

    // UPLOAD POLICY DOCUMENT
    @PostMapping("/upload/{policyId}")
    public String uploadDocument(
            @PathVariable Long policyId,
            @RequestParam MultipartFile file
    ) throws IOException {
        service.uploadPolicyDocument(policyId, file);
        return "Document uploaded successfully";
    }
}
