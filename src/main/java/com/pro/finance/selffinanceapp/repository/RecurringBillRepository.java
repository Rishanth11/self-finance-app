package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.RecurringBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringBillRepository
        extends JpaRepository<RecurringBill, Long> {

    List<RecurringBill> findByUserId(Long userId);

    List<RecurringBill> findByNextDueDateBetween(
            LocalDate start,
            LocalDate end
    );
}

