package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    public Expense saveExpense(Expense expense) {
        return repo.save(expense);
    }

    public List<Expense> getExpensesByUser(Long userId) {
        return repo.findByUserIdOrderByExpenseDateDesc(userId);
    }

    public BigDecimal getTotalExpense(Long userId) {
        return repo.getTotalExpense(userId);
    }

    public Expense updateExpense(Long id, Expense updatedExpense) {

        Expense existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        existing.setCategory(updatedExpense.getCategory());
        existing.setAmount(updatedExpense.getAmount());
        existing.setExpenseDate(updatedExpense.getExpenseDate());
        existing.setDescription(updatedExpense.getDescription());

        return repo.save(existing);
    }

    public void deleteExpense(Long id) {
        repo.deleteById(id);
    }
}