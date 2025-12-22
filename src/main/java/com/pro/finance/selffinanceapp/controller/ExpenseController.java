package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.ExpenseDTO;
import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService service;

    // Create Expense
    @PostMapping
    public ExpenseDTO addExpense(@RequestBody ExpenseDTO expenseDTO) {
        Expense expense = mapToEntity(expenseDTO);
        Expense savedExpense = service.saveExpense(expense);
        return mapToDTO(savedExpense);
    }

    // Get all expenses for a user
    @GetMapping("/{userId}")
    public List<ExpenseDTO> getExpenses(@PathVariable Long userId) {
        List<Expense> expenses = service.getExpensesByUser(userId);
        return expenses.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Update expense by ID
    @PutMapping("/{id}")
    public ExpenseDTO updateExpense(@PathVariable Long id, @RequestBody ExpenseDTO expenseDTO) {
        Expense existingExpense = service.getExpensesByUser(expenseDTO.getUserId())
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Update fields
        existingExpense.setCategory(expenseDTO.getCategory());
        existingExpense.setAmount(expenseDTO.getAmount());
        existingExpense.setExpenseDate(expenseDTO.getExpenseDate());
        existingExpense.setDescription(expenseDTO.getDescription());

        Expense updatedExpense = service.saveExpense(existingExpense);
        return mapToDTO(updatedExpense);
    }

    // Delete expense by ID
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        service.deleteExpense(id);
    }

    // Utility method to map entity to DTO
    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setUserId(expense.getUserId());
        dto.setCategory(expense.getCategory());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setDescription(expense.getDescription());
        return dto;
    }

    // Utility method to map DTO to entity
    private Expense mapToEntity(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setUserId(dto.getUserId());
        expense.setCategory(dto.getCategory());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setDescription(dto.getDescription());
        return expense;
    }
}
