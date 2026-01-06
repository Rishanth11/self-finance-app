package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.SipInvestment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SipInvestmentRepository extends JpaRepository<SipInvestment, Long> {
    List<SipInvestment> findByUserId(Long userId);
}
