package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.InsurancePolicy;
import com.pro.finance.selffinanceapp.repository.InsurancePolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class InsuranceService {

    @Autowired
    private InsurancePolicyRepository repository;

    public InsurancePolicy addPolicy(InsurancePolicy policy) {
        policy.setActive(true);
        return repository.save(policy);
    }

    public List<InsurancePolicy> getUserPolicies(String email) {
        return repository.findByUserEmail(email);
    }

    public List<InsurancePolicy> getPremiumReminders() {
        LocalDate now = LocalDate.now();
        return repository.findByNextPremiumDateBetween(
                now, now.plusDays(7));
    }

    public List<InsurancePolicy> getMaturityPolicies() {
        LocalDate now = LocalDate.now();
        return repository.findByMaturityDateBetween(
                now, now.plusDays(30));
    }

    public void uploadPolicyDocument(Long policyId, MultipartFile file)
            throws IOException {

        String dir = "uploads/insurance/";
        new File(dir).mkdirs();

        String path = dir + policyId + "_" + file.getOriginalFilename();
        file.transferTo(new File(path));

        InsurancePolicy policy = repository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        policy.setDocumentPath(path);
        repository.save(policy);
    }
}
