package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanIdOrderByMonthNo(Long loanId);

    @Query("SELECT COALESCE(SUM(e.emiAmount),0) FROM EmiSchedule e WHERE e.loanId = :loanId AND e.paid = true")
    double getTotalPaid(@Param("loanId") Long loanId);
}
