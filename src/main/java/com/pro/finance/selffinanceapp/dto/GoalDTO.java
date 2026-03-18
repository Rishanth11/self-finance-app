package com.pro.finance.selffinanceapp.dto;

import com.pro.finance.selffinanceapp.model.GoalCategory;
import com.pro.finance.selffinanceapp.model.GoalPriority;
import com.pro.finance.selffinanceapp.model.GoalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String name;
        private String description;
        private GoalCategory category;
        private GoalPriority priority;
        private BigDecimal targetAmount;
        private BigDecimal monthlyContribution;
        private LocalDate targetDate;
        private LocalDate startDate;
        private String linkedAccount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private GoalCategory category;
        private GoalPriority priority;
        private GoalStatus status;
        private BigDecimal targetAmount;
        private BigDecimal savedAmount;
        private BigDecimal remainingAmount;
        private BigDecimal monthlyContribution;
        private double progressPercentage;
        private LocalDate targetDate;
        private LocalDate startDate;
        private LocalDate projectedCompletionDate;
        private String linkedAccount;
        private long monthsRemaining;
        private boolean onTrack;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContributionRequest {
        private Long goalId;
        private BigDecimal amount;
        private java.time.LocalDate contributionDate;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContributionResponse {
        private Long id;
        private Long goalId;
        private String goalName;
        private BigDecimal amount;
        private LocalDate contributionDate;
        private String note;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private long totalGoals;
        private long activeGoals;
        private long completedGoals;
        private BigDecimal totalTargetAmount;
        private BigDecimal totalSavedAmount;
        private BigDecimal totalRemainingAmount;
        private double overallProgress;
    }
}