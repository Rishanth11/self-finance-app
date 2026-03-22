package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.BudgetExpenseRequestDTO;
import com.pro.finance.selffinanceapp.dto.BudgetRequestDTO;
import com.pro.finance.selffinanceapp.dto.BudgetSummaryDTO;
import com.pro.finance.selffinanceapp.model.Budget;
import com.pro.finance.selffinanceapp.model.BudgetAlert;
import com.pro.finance.selffinanceapp.model.BudgetExpense;
import com.pro.finance.selffinanceapp.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody BudgetRequestDTO request) {
        return ResponseEntity.ok(budgetService.createBudget(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getBudgetsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(budgetService.getBudgetsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id,
                                               @RequestBody BudgetRequestDTO request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/expenses")
    public ResponseEntity<BudgetExpense> addExpense(@PathVariable Long id,
                                                    @RequestBody BudgetExpenseRequestDTO request) {
        return ResponseEntity.ok(budgetService.addExpense(id, request));
    }

    @GetMapping("/{id}/expenses")
    public ResponseEntity<List<BudgetExpense>> getExpenses(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getExpenses(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<BudgetSummaryDTO> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetSummary(id));
    }

    @GetMapping("/{id}/alerts")
    public ResponseEntity<List<BudgetAlert>> getAlerts(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getAlerts(id));
    }

    @PutMapping("/alerts/{alertId}/seen")
    public ResponseEntity<Void> markAlertSeen(@PathVariable Long alertId) {
        budgetService.markAlertSeen(alertId);
        return ResponseEntity.ok().build();
    }
}