package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    // 🔹 Get incomes by user ordered by date (latest first)
    List<Income> findByUserOrderByDateDesc(User user);

    // 🔹 Get total income (used in FinancialHealthService)
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user.id = :userId")
    BigDecimal getTotalIncome(@Param("userId") Long userId);
}
