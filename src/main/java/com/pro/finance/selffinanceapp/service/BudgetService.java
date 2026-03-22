package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.BudgetExpenseRequestDTO;
import com.pro.finance.selffinanceapp.dto.BudgetRequestDTO;
import com.pro.finance.selffinanceapp.dto.BudgetSummaryDTO;
import com.pro.finance.selffinanceapp.dto.CategorySummaryDTO;
import com.pro.finance.selffinanceapp.model.Budget;
import com.pro.finance.selffinanceapp.model.BudgetAlert;
import com.pro.finance.selffinanceapp.model.BudgetCategory;
import com.pro.finance.selffinanceapp.model.BudgetExpense;
import com.pro.finance.selffinanceapp.repository.BudgetAlertRepository;
import com.pro.finance.selffinanceapp.repository.BudgetCategoryRepository;
import com.pro.finance.selffinanceapp.repository.BudgetExpenseRepository;
import com.pro.finance.selffinanceapp.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository categoryRepository;
    private final BudgetExpenseRepository expenseRepository;
    private final BudgetAlertRepository alertRepository;

    @Transactional
    public Budget createBudget(BudgetRequestDTO request) {
        Budget budget = Budget.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .budgetType(request.getBudgetType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalAmount(request.getTotalAmount())
                .goalId(request.getGoalId())
                .build();

        Budget savedBudget = budgetRepository.save(budget);

        if (request.getCategories() != null) {
            request.getCategories().forEach(catDTO -> {
                BudgetCategory category = BudgetCategory.builder()
                        .budget(savedBudget)
                        .categoryName(catDTO.getCategoryName())
                        .allocatedAmount(catDTO.getAllocatedAmount())
                        .alertThreshold(catDTO.getAlertThreshold() != null ? catDTO.getAlertThreshold() : 80)
                        .spentAmount(BigDecimal.ZERO)
                        .build();
                categoryRepository.save(category);
            });
        }
        return savedBudget;
    }

    public List<Budget> getBudgetsByUser(Long userId) {
        return budgetRepository.findByUserIdWithCategories(userId);
    }

    public Budget getBudgetById(Long id) {
        return budgetRepository.findByIdWithCategories(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
    }

    @Transactional
    public Budget updateBudget(Long id, BudgetRequestDTO request) {
        Budget budget = getBudgetById(id);
        budget.setName(request.getName());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setTotalAmount(request.getTotalAmount());
        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    @Transactional
    public BudgetExpense addExpense(Long budgetId, BudgetExpenseRequestDTO request) {
        Budget budget = getBudgetById(budgetId);

        BudgetCategory category = null;
        if (request.getBudgetCategoryId() != null) {
            category = categoryRepository.findById(request.getBudgetCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            category.setSpentAmount(category.getSpentAmount().add(request.getAmount()));
            categoryRepository.save(category);

            checkAndTriggerAlerts(budget, category);
        }

        return expenseRepository.save(BudgetExpense.builder()
                .budget(budget)
                .budgetCategory(category)
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .build());
    }

    private void checkAndTriggerAlerts(Budget budget, BudgetCategory category) {
        if (category.getAllocatedAmount().compareTo(BigDecimal.ZERO) == 0) return;

        double percentage = category.getSpentAmount()
                .divide(category.getAllocatedAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        BudgetAlert.AlertType alertType = null;
        String message = null;

        if (percentage >= 100) {
            alertType = BudgetAlert.AlertType.EXCEEDED;
            message = "Budget EXCEEDED for category: " + category.getCategoryName();
        } else if (percentage >= category.getAlertThreshold()) {
            alertType = BudgetAlert.AlertType.WARNING;
            message = String.format("%.0f%% of budget used for category: %s", percentage, category.getCategoryName());
        }

        if (alertType != null) {
            alertRepository.save(BudgetAlert.builder()
                    .budget(budget)
                    .budgetCategoryId(category.getId())
                    .alertType(alertType)
                    .message(message)
                    .isSeen(false)
                    .build());
        }
    }

    public BudgetSummaryDTO getBudgetSummary(Long budgetId) {
        Budget budget = getBudgetById(budgetId);
        List<BudgetCategory> categories = categoryRepository.findByBudgetId(budgetId);

        BigDecimal totalSpent = categories.stream()
                .map(BudgetCategory::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = budget.getTotalAmount().subtract(totalSpent);

        double spentPct = budget.getTotalAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                totalSpent.divide(budget.getTotalAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();

        List<CategorySummaryDTO> catSummaries = categories.stream().map(cat -> {
            BigDecimal remaining = cat.getAllocatedAmount().subtract(cat.getSpentAmount());
            double catPct = cat.getAllocatedAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    cat.getSpentAmount().divide(cat.getAllocatedAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();

            String status = catPct >= 100 ? "EXCEEDED" : catPct >= cat.getAlertThreshold() ? "WARNING" : "SAFE";

            return CategorySummaryDTO.builder()
                    .categoryId(cat.getId())
                    .categoryName(cat.getCategoryName())
                    .allocatedAmount(cat.getAllocatedAmount())
                    .spentAmount(cat.getSpentAmount())
                    .remainingAmount(remaining)
                    .spentPercentage(catPct)
                    .status(status)
                    .build();
        }).collect(Collectors.toList());

        long unreadAlerts = alertRepository.countByBudgetIdAndIsSeenFalse(budgetId);

        return BudgetSummaryDTO.builder()
                .budgetId(budget.getId())
                .budgetName(budget.getName())
                .totalBudget(budget.getTotalAmount())
                .totalSpent(totalSpent)
                .totalRemaining(totalRemaining)
                .spentPercentage(spentPct)
                .categorySummaries(catSummaries)
                .unreadAlerts(unreadAlerts)
                .build();
    }

    public List<BudgetAlert> getAlerts(Long budgetId) {
        return alertRepository.findByBudgetId(budgetId);
    }

    public void markAlertSeen(Long alertId) {
        BudgetAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setIsSeen(true);
        alertRepository.save(alert);
    }

    public List<BudgetExpense> getExpenses(Long budgetId) {
        return expenseRepository.findByBudgetId(budgetId);
    }
}