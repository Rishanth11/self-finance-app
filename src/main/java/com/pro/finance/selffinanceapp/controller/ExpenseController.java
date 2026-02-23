package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.ExpenseDTO;
import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    // TEMP USER (until JWT extraction implemented properly)
    private final Long USER_ID = 1L;

    // CREATE
    @PostMapping
    public ExpenseDTO addExpense(@RequestBody ExpenseDTO dto) {
        Expense expense = mapToEntity(dto);
        expense.setUserId(USER_ID);
        Expense saved = service.saveExpense(expense);
        return mapToDTO(saved);
    }

    // ✅ FIXED GET (No userId in path)
    @GetMapping
    public List<ExpenseDTO> getExpenses() {
        return service.getExpensesByUser(USER_ID)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // GET TOTAL
    @GetMapping("/total")
    public BigDecimal getTotal() {
        return service.getTotalExpense(USER_ID);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ExpenseDTO updateExpense(@PathVariable Long id,
                                    @RequestBody ExpenseDTO dto) {
        Expense updated = service.updateExpense(id, mapToEntity(dto));
        return mapToDTO(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        service.deleteExpense(id);
    }

    // MAPPER METHODS
    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setUserId(expense.getUserId());
        dto.setCategory(expense.getCategory());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setDescription(expense.getDescription());
        return dto;
    }

    private Expense mapToEntity(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setId(dto.getId());
        expense.setCategory(dto.getCategory());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setDescription(dto.getDescription());
        return expense;
    }

    @GetMapping("/filter")
    public List<ExpenseDTO> getExpensesByMonth(
            @RequestParam int year,
            @RequestParam int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return service.getByDateRange(USER_ID, start, end)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
}