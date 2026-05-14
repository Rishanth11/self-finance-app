package com.rishanth.flux360.repository;
import com.rishanth.flux360.model.BudgetExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetExpenseRepository extends JpaRepository<BudgetExpense, Long> {
    List<BudgetExpense> findByBudgetId(Long budgetId);
    List<BudgetExpense> findByBudgetCategoryId(Long budgetCategoryId);
}