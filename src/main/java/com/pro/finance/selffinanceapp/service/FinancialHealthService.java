package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.FinancialHealthDTO;
import com.pro.finance.selffinanceapp.repository.ExpenseRepository;
import com.pro.finance.selffinanceapp.repository.IncomeRepository;
import org.springframework.stereotype.Service;

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

        double income = incomeRepository.getTotalIncome(userId);
        double expense = expenseRepository.getTotalExpense(userId);

        double savings = income - expense;
        double score = income == 0 ? 0 : (savings / income) * 100;

        String status;
        if (score >= 80) status = "EXCELLENT";
        else if (score >= 60) status = "GOOD";
        else if (score >= 40) status = "AVERAGE";
        else status = "POOR";

        return new FinancialHealthDTO(
                income,
                expense,
                savings,
                Math.round(score),
                status
        );
    }
}
