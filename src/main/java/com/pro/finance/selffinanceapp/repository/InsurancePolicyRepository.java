package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
