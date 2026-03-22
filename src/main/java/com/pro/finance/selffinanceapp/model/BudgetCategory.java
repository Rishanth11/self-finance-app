package com.pro.finance.selffinanceapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budget_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FIX: @JsonBackReference prevents infinite recursion (Budget -> Category -> Budget -> ...)
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    private String categoryName;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount = BigDecimal.ZERO;
    private Integer alertThreshold = 80;
}