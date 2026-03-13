package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    // SAVE
    public Expense saveExpense(Expense expense) {
        return repo.save(expense);
    }

    // GET USER EXPENSES
    public List<Expense> getExpensesByUser(User user) {
        return repo.findByUserOrderByExpenseDateDesc(user);
    }

    // TOTAL EXPENSE
    public BigDecimal getTotalExpense(Long userId) {
        return repo.getTotalExpense(userId);
    }

    // UPDATE
    public Expense updateExpense(Long id, Expense updatedExpense) {

        Expense existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        existing.setCategory(updatedExpense.getCategory());
        existing.setAmount(updatedExpense.getAmount());
        existing.setExpenseDate(updatedExpense.getExpenseDate());
        existing.setDescription(updatedExpense.getDescription());

        return repo.save(existing);
    }

    // DELETE
    public void deleteExpense(Long id) {
        repo.deleteById(id);
    }

    // FILTER BY DATE RANGE
    public List<Expense> getByDateRange(User user,
                                        LocalDate start,
                                        LocalDate end) {

        return repo.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                user, start, end);
    }
}