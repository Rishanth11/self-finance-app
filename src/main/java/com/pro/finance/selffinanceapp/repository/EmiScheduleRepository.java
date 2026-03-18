package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanIdOrderByMonthNo(Long loanId);

    // Find first unpaid EMI (used to enforce sequential payment)
    Optional<EmiSchedule> findFirstByLoanIdAndPaidFalseOrderByMonthNoAsc(Long loanId);

    // Sum of all paid EMI amounts for a loan
    @Query("SELECT COALESCE(SUM(e.emiAmount), 0) FROM EmiSchedule e WHERE e.loanId = :loanId AND e.paid = true")
    double getTotalPaid(@Param("loanId") Long loanId);

    // Sum of principal paid so far
    @Query("SELECT COALESCE(SUM(e.principalComponent), 0) FROM EmiSchedule e WHERE e.loanId = :loanId AND e.paid = true")
    double getTotalPrincipalPaid(@Param("loanId") Long loanId);

    // Sum of interest paid so far
    @Query("SELECT COALESCE(SUM(e.interestComponent), 0) FROM EmiSchedule e WHERE e.loanId = :loanId AND e.paid = true")
    double getTotalInterestPaid(@Param("loanId") Long loanId);

    // Count of paid EMIs
    int countByLoanIdAndPaidTrue(Long loanId);

    // Upcoming unpaid EMIs (for reminder / upcoming view)
    List<EmiSchedule> findByLoanIdAndPaidFalseOrderByMonthNoAsc(Long loanId);
}