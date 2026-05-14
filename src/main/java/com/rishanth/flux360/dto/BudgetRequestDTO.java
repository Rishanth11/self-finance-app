package com.rishanth.flux360.dto;

import com.rishanth.flux360.model.Budget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequestDTO {
    private Long userId;
    private String name;
    private Budget.BudgetType budgetType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private Long goalId;
    private List<BudgetCategoryDTO> categories;
}