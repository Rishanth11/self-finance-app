package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.SipInvestment;
import com.pro.finance.selffinanceapp.model.SipTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SipTransactionRepository extends JpaRepository<SipTransaction, Long> {

    List<SipTransaction> findBySip(SipInvestment sip);

    boolean existsBySipAndInvestDate(SipInvestment sip, LocalDate investDate);
}