package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Goal;
import com.pro.finance.selffinanceapp.model.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByStatusOrderByPriorityAscTargetDateAsc(GoalStatus status);

    List<Goal> findAllByOrderByPriorityAscTargetDateAsc();

    List<Goal> findByStatus(GoalStatus status);

    @Query("SELECT COALESCE(SUM(g.targetAmount), 0) FROM Goal g WHERE g.status = 'ACTIVE'")
    BigDecimal sumTargetAmountByActiveGoals();

    @Query("SELECT COALESCE(SUM(g.savedAmount), 0) FROM Goal g WHERE g.status = 'ACTIVE'")
    BigDecimal sumSavedAmountByActiveGoals();

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.status = 'COMPLETED'")
    long countCompletedGoals();
}