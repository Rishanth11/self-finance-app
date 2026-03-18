package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.GoalDTO;
import com.pro.finance.selffinanceapp.model.GoalStatus;
import com.pro.finance.selffinanceapp.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GoalController {

    private final GoalService goalService;

    // ─── GOALS ────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<GoalDTO.Response> createGoal(@RequestBody GoalDTO.Request request) {
        return ResponseEntity.ok(goalService.createGoal(request));
    }

    @GetMapping
    public ResponseEntity<List<GoalDTO.Response>> getAllGoals() {
        return ResponseEntity.ok(goalService.getAllGoals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalDTO.Response> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<GoalDTO.Response>> getGoalsByStatus(@PathVariable GoalStatus status) {
        return ResponseEntity.ok(goalService.getGoalsByStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalDTO.Response> updateGoal(
            @PathVariable Long id,
            @RequestBody GoalDTO.Request request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<GoalDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestParam GoalStatus status) {
        return ResponseEntity.ok(goalService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    // ─── CONTRIBUTIONS ────────────────────────────────────────────

    @PostMapping("/contributions")
    public ResponseEntity<GoalDTO.ContributionResponse> addContribution(
            @RequestBody GoalDTO.ContributionRequest request) {
        return ResponseEntity.ok(goalService.addContribution(request));
    }

    @GetMapping("/{goalId}/contributions")
    public ResponseEntity<List<GoalDTO.ContributionResponse>> getContributions(
            @PathVariable Long goalId) {
        return ResponseEntity.ok(goalService.getContributionsByGoal(goalId));
    }

    @DeleteMapping("/contributions/{contributionId}")
    public ResponseEntity<Void> deleteContribution(@PathVariable Long contributionId) {
        goalService.deleteContribution(contributionId);
        return ResponseEntity.noContent().build();
    }

    // ─── SUMMARY ──────────────────────────────────────────────────

    @GetMapping("/summary")
    public ResponseEntity<GoalDTO.Summary> getSummary() {
        return ResponseEntity.ok(goalService.getSummary());
    }
}