package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

        // ✅ GET TOTAL EXPENSE
        @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId")
        BigDecimal getTotalExpense(@Param("userId") Long userId);

        // ✅ GET EXPENSES BY USER ORDERED BY DATE DESC
        List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);
}
