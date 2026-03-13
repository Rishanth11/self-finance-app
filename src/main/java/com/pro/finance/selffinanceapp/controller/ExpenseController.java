package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.ExpenseDTO;
import com.pro.finance.selffinanceapp.model.Expense;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.ExpenseService;
import com.pro.finance.selffinanceapp.service.UserService;
import org.springframework.security.core.Authentication;
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
    private final UserService userService;

    public ExpenseController(ExpenseService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // CREATE
    @PostMapping
    public ExpenseDTO addExpense(@RequestBody ExpenseDTO dto,
                                 Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByEmail(username);

        Expense expense = mapToEntity(dto);
        expense.setUser(user);

        Expense saved = service.saveExpense(expense);
        return mapToDTO(saved);
    }

    // GET ALL
    @GetMapping
    public List<ExpenseDTO> getExpenses(Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByEmail(username);

        return service.getExpensesByUser(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // TOTAL EXPENSE
    @GetMapping("/total")
    public BigDecimal getTotal(Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByEmail(username);

        return service.getTotalExpense(user.getId());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ExpenseDTO updateExpense(@PathVariable Long id,
                                    @RequestBody ExpenseDTO dto,
                                    Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByEmail(username);

        Expense expense = mapToEntity(dto);
        expense.setUser(user);

        Expense updated = service.updateExpense(id, expense);
        return mapToDTO(updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        service.deleteExpense(id);
    }

    // FILTER BY MONTH
    @GetMapping("/filter")
    public List<ExpenseDTO> getExpensesByMonth(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userService.findByEmail(username);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return service.getByDateRange(user, start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // DTO MAPPERS

    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
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
}