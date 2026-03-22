package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    private Long budgetCategoryId;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    private String message;
    private LocalDateTime triggeredAt;
    private Boolean isSeen = false;

    @PrePersist
    protected void onCreate() {
        triggeredAt = LocalDateTime.now();
    }

    public enum AlertType {
        WARNING, EXCEEDED
    }
}