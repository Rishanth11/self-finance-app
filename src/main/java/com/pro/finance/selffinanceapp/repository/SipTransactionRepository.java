package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.SipTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SipTransactionRepository
        extends JpaRepository<SipTransaction, Long> {

    List<SipTransaction> findBySipId(Long sipId);

    boolean existsBySipIdAndInvestDate(Long sipId, LocalDate investDate);
}
