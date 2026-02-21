package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.FinancialHealthDTO;
import com.pro.finance.selffinanceapp.repository.ExpenseRepository;
import com.pro.finance.selffinanceapp.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FinancialHealthService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public FinancialHealthService(IncomeRepository incomeRepository,
                                  ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    public FinancialHealthDTO calculateHealthScore(Long userId) {

        BigDecimal income = incomeRepository.getTotalIncome(userId);
        BigDecimal expense = expenseRepository.getTotalExpense(userId);

        if (income == null) income = BigDecimal.ZERO;
        if (expense == null) expense = BigDecimal.ZERO;

        // savings = income - expense
        BigDecimal savings = income.subtract(expense);

        BigDecimal score = BigDecimal.ZERO;

        if (income.compareTo(BigDecimal.ZERO) > 0) {
            score = savings
                    .divide(income, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        String status;
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) status = "EXCELLENT";
        else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) status = "GOOD";
        else if (score.compareTo(BigDecimal.valueOf(40)) >= 0) status = "AVERAGE";
        else status = "POOR";

        return new FinancialHealthDTO(
                income,
                expense,
                savings,
                score.setScale(0, RoundingMode.HALF_UP).intValue(),
                status
        );
    }
}
