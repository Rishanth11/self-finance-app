package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.UserDTO;
import com.pro.finance.selffinanceapp.dto.UserStatsDTO;
import com.pro.finance.selffinanceapp.service.AdminUserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")           // entire controller is admin-only
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /* ── GET /api/admin/users  →  list all users ─────────────────────── */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    /* ── GET /api/admin/users/stats  →  stats card data ─────────────── */
    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getStats() {
        return ResponseEntity.ok(adminUserService.getUserStats());
    }

    /* ── PUT /api/admin/users/{id}/block  →  block user ─────────────── */
    @PutMapping("/{id}/block")
    public ResponseEntity<UserDTO> blockUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminUserService.blockUser(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* ── PUT /api/admin/users/{id}/unblock  →  unblock user ─────────── */
    @PutMapping("/{id}/unblock")
    public ResponseEntity<UserDTO> unblockUser(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminUserService.unblockUser(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* ── DELETE /api/admin/users/{id}  →  delete account ────────────── */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            adminUserService.deleteUser(id);
            return ResponseEntity.noContent().build();     // 204
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}