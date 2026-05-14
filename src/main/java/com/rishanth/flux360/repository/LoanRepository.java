package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.Loan;
import com.rishanth.flux360.model.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId);

    List<Loan> findByUserIdAndClosed(Long userId, boolean closed);

    List<Loan> findByUserIdAndLoanType(Long userId, LoanType loanType);
}