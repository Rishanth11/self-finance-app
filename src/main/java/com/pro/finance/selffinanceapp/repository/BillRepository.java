package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Bill;
import com.pro.finance.selffinanceapp.model.BillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByUserIdOrderByNextDueDateAsc(Long userId);

    List<Bill> findByUserIdAndCategoryOrderByNextDueDateAsc(Long userId, BillCategory category);

    // Bills with nextDueDate before today and not paid (overdue)
    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.paid = false AND b.nextDueDate < :today")
    List<Bill> findOverdue(@Param("userId") Long userId, @Param("today") LocalDate today);

    // Bills with nextDueDate between two dates and not paid
    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.paid = false AND b.nextDueDate BETWEEN :from AND :to")
    List<Bill> findDueBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}