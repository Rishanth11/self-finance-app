package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository repo;

    // Save a new expense
    public Expense saveExpense(Expense expense) {
        return repo.save(expense);
    }

    // Get all expenses by user
    public List<Expense> getExpensesByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    // Delete expense by ID
    public void deleteExpense(Long id) {
        repo.deleteById(id);
    }

    // Update expense by ID
    public Expense updateExpense(Long id, Expense updatedExpense) {
        Expense existingExpense = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        existingExpense.setCategory(updatedExpense.getCategory());
        existingExpense.setAmount(updatedExpense.getAmount());
        existingExpense.setExpenseDate(updatedExpense.getExpenseDate());
        existingExpense.setDescription(updatedExpense.getDescription());

        return repo.save(existingExpense);
    }
}
