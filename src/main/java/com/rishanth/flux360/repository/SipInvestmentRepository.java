package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.SipInvestment;
import com.rishanth.flux360.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SipInvestmentRepository extends JpaRepository<SipInvestment, Long> {

    List<SipInvestment> findByUser(User user);

    // Fix 2: efficient query — only active SIPs, no in-memory filtering
    List<SipInvestment> findByActiveTrue();
}