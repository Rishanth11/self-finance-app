package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Fetch budgets WITH their categories in one query — fixes the lazy loading issue
    @Query("SELECT DISTINCT b FROM Budget b LEFT JOIN FETCH b.categories WHERE b.userId = :userId")
    List<Budget> findByUserIdWithCategories(@Param("userId") Long userId);

    @Query("SELECT DISTINCT b FROM Budget b LEFT JOIN FETCH b.categories WHERE b.id = :id")
    Optional<Budget> findByIdWithCategories(@Param("id") Long id);

    List<Budget> findByUserId(Long userId);
    List<Budget> findByUserIdAndBudgetType(Long userId, Budget.BudgetType budgetType);
}