package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    // ✅ SAVE EXPENSE
    public Expense saveExpense(Expense expense) {
        return repo.save(expense);
    }

    // ✅ GET EXPENSES BY USER ID
    public List<Expense> getExpensesByUser(Long userId) {
        return repo.findByUserIdOrderByExpenseDateDesc(userId);
    }


    // ✅ DELETE EXPENSE
    public void deleteExpense(Long id) {
        repo.deleteById(id);
    }

    // ✅ UPDATE EXPENSE
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
