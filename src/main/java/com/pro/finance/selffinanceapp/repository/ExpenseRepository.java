package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

        @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.user.id = :userId")
        BigDecimal getTotalExpense(@Param("userId") Long userId);

        List<Expense> findByUserOrderByExpenseDateDesc(User user);

        List<Expense> findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                User user,
                LocalDate start,
                LocalDate end
        );
}