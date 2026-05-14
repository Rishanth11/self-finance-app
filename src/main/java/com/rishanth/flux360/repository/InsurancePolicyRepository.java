package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InsurancePolicyRepository
        extends JpaRepository<InsurancePolicy, Long> {

    List<InsurancePolicy> findByUserEmail(String email);

    List<InsurancePolicy> findByNextPremiumDateBetween(
            LocalDate start, LocalDate end);

    List<InsurancePolicy> findByMaturityDateBetween(
            LocalDate start, LocalDate end);
}
