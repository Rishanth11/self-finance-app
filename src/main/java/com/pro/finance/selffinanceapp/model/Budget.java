package com.pro.finance.selffinanceapp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String name;

    @Enumerated(EnumType.STRING)
    private BudgetType budgetType;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private Long goalId;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // FIX: Use EAGER fetch so categories always load with budget
    // @JsonManagedReference prevents infinite recursion when serializing to JSON
    @JsonManagedReference
    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BudgetCategory> categories;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BudgetType {
        MONTHLY_CATEGORY, CUSTOM_RANGE, RULE_50_30_20, GOAL_LINKED
    }
}