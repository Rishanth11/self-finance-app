package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
        List<Expense> findByUserId(Long userId);
}


