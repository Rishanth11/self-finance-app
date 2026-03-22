package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.BudgetAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {
    List<BudgetAlert> findByBudgetId(Long budgetId);
    List<BudgetAlert> findByBudgetIdAndIsSeenFalse(Long budgetId);
    long countByBudgetIdAndIsSeenFalse(Long budgetId);
}