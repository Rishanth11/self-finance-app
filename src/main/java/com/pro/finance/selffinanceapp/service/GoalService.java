package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.GoalDTO;
import com.pro.finance.selffinanceapp.model.Goal;
import com.pro.finance.selffinanceapp.model.GoalContribution;
import com.pro.finance.selffinanceapp.model.GoalStatus;
import com.pro.finance.selffinanceapp.repository.GoalContributionRepository;
import com.pro.finance.selffinanceapp.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;

    // ─── GOAL CRUD ────────────────────────────────────────────────

    @Transactional
    public GoalDTO.Response createGoal(GoalDTO.Request request) {
        Goal goal = Goal.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .targetAmount(request.getTargetAmount())
                .monthlyContribution(request.getMonthlyContribution())
                .targetDate(request.getTargetDate())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .linkedAccount(request.getLinkedAccount())
                .status(GoalStatus.ACTIVE)
                .savedAmount(BigDecimal.ZERO)
                .build();
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalDTO.Response updateGoal(Long id, GoalDTO.Request request) {
        Goal goal = findGoalById(id);
        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setCategory(request.getCategory());
        goal.setPriority(request.getPriority());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setMonthlyContribution(request.getMonthlyContribution());
        goal.setTargetDate(request.getTargetDate());
        goal.setLinkedAccount(request.getLinkedAccount());
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalDTO.Response updateStatus(Long id, GoalStatus status) {
        Goal goal = findGoalById(id);
        goal.setStatus(status);
        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long id) {
        goalRepository.deleteById(id);
    }

    public GoalDTO.Response getGoalById(Long id) {
        return toResponse(findGoalById(id));
    }

    public List<GoalDTO.Response> getAllGoals() {
        return goalRepository.findAllByOrderByPriorityAscTargetDateAsc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GoalDTO.Response> getGoalsByStatus(GoalStatus status) {
        return goalRepository.findByStatus(status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── CONTRIBUTIONS ────────────────────────────────────────────

    @Transactional
    public GoalDTO.ContributionResponse addContribution(GoalDTO.ContributionRequest request) {
        Goal goal = findGoalById(request.getGoalId());

        GoalContribution contribution = GoalContribution.builder()
                .goal(goal)
                .amount(request.getAmount())
                .contributionDate(request.getContributionDate() != null
                        ? request.getContributionDate() : LocalDate.now())
                .note(request.getNote())
                .build();

        contributionRepository.save(contribution);

        // Update savedAmount on goal
        BigDecimal newSaved = goal.getSavedAmount().add(request.getAmount());
        goal.setSavedAmount(newSaved);

        // Auto-complete if target reached
        if (newSaved.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }

        goalRepository.save(goal);
        return toContributionResponse(contribution);
    }

    @Transactional
    public void deleteContribution(Long contributionId) {
        GoalContribution contribution = contributionRepository.findById(contributionId)
                .orElseThrow(() -> new RuntimeException("Contribution not found: " + contributionId));

        Goal goal = contribution.getGoal();
        goal.setSavedAmount(goal.getSavedAmount().subtract(contribution.getAmount()).max(BigDecimal.ZERO));

        // Reactivate if status was COMPLETED and amount drops below target
        if (goal.getStatus() == GoalStatus.COMPLETED
                && goal.getSavedAmount().compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(GoalStatus.ACTIVE);
        }

        goalRepository.save(goal);
        contributionRepository.delete(contribution);
    }

    public List<GoalDTO.ContributionResponse> getContributionsByGoal(Long goalId) {
        return contributionRepository.findByGoalIdOrderByContributionDateDesc(goalId)
                .stream().map(this::toContributionResponse).collect(Collectors.toList());
    }

    // ─── SUMMARY ──────────────────────────────────────────────────

    public GoalDTO.Summary getSummary() {
        List<Goal> all = goalRepository.findAll();
        long active = all.stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).count();
        long completed = all.stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();

        BigDecimal totalTarget = all.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .map(Goal::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSaved = all.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .map(Goal::getSavedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalTarget.subtract(totalSaved).max(BigDecimal.ZERO);

        double overallProgress = totalTarget.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                : totalSaved.multiply(BigDecimal.valueOf(100))
                .divide(totalTarget, 2, RoundingMode.HALF_UP).doubleValue();

        return GoalDTO.Summary.builder()
                .totalGoals(all.size())
                .activeGoals(active)
                .completedGoals(completed)
                .totalTargetAmount(totalTarget)
                .totalSavedAmount(totalSaved)
                .totalRemainingAmount(totalRemaining)
                .overallProgress(overallProgress)
                .build();
    }

    // ─── MAPPERS ──────────────────────────────────────────────────

    private GoalDTO.Response toResponse(Goal goal) {
        LocalDate today = LocalDate.now();
        long monthsRemaining = ChronoUnit.MONTHS.between(today, goal.getTargetDate());
        if (monthsRemaining < 0) monthsRemaining = 0;

        // Projected completion based on monthly contribution
        LocalDate projectedDate = null;
        boolean onTrack = false;
        BigDecimal remaining = goal.getRemainingAmount();

        if (goal.getMonthlyContribution() != null
                && goal.getMonthlyContribution().compareTo(BigDecimal.ZERO) > 0
                && remaining.compareTo(BigDecimal.ZERO) > 0) {

            long monthsNeeded = remaining.divide(goal.getMonthlyContribution(), 0, RoundingMode.CEILING).longValue();
            projectedDate = today.plusMonths(monthsNeeded);
            onTrack = !projectedDate.isAfter(goal.getTargetDate());
        } else if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            projectedDate = today;
            onTrack = true;
        }

        return GoalDTO.Response.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .category(goal.getCategory())
                .priority(goal.getPriority())
                .status(goal.getStatus())
                .targetAmount(goal.getTargetAmount())
                .savedAmount(goal.getSavedAmount())
                .remainingAmount(remaining)
                .monthlyContribution(goal.getMonthlyContribution())
                .progressPercentage(goal.getProgressPercentage())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .projectedCompletionDate(projectedDate)
                .linkedAccount(goal.getLinkedAccount())
                .monthsRemaining(monthsRemaining)
                .onTrack(onTrack)
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    private GoalDTO.ContributionResponse toContributionResponse(GoalContribution c) {
        return GoalDTO.ContributionResponse.builder()
                .id(c.getId())
                .goalId(c.getGoal().getId())
                .goalName(c.getGoal().getName())
                .amount(c.getAmount())
                .contributionDate(c.getContributionDate())
                .note(c.getNote())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private Goal findGoalById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goal not found: " + id));
    }
}