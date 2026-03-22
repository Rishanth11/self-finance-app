package com.pro.finance.selffinanceapp.repository;
import com.pro.finance.selffinanceapp.model.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, Long> {
    List<BudgetCategory> findByBudgetId(Long budgetId);
}