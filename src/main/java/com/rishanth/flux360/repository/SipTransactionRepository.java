package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.SipInvestment;
import com.rishanth.flux360.model.SipTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SipTransactionRepository extends JpaRepository<SipTransaction, Long> {

    List<SipTransaction> findBySip(SipInvestment sip);

    boolean existsBySipAndInvestDate(SipInvestment sip, LocalDate investDate);
}