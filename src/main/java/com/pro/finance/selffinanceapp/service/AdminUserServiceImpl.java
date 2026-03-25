package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.UserDTO;
import com.pro.finance.selffinanceapp.dto.UserStatsDTO;
import com.pro.finance.selffinanceapp.model.Role;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.model.UserStatus;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;   // ✅ added

    public AdminUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* ── Get all users ──────────────────────────────────────────────── */
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    /* ── Stats ──────────────────────────────────────────────────────── */
    @Override
    public UserStatsDTO getUserStats() {
        long total   = userRepository.count();
        long active  = userRepository.countByStatus(UserStatus.ACTIVE);
        long blocked = userRepository.countByStatus(UserStatus.BLOCKED);
        long admins  = userRepository.countByRole(Role.ROLE_ADMIN);
        return new UserStatsDTO(total, active, blocked, admins);
    }

    /* ── Block user ─────────────────────────────────────────────────── */
    @Override
    @Transactional
    public UserDTO blockUser(Long userId) {
        User user = findUserById(userId);
        user.setStatus(UserStatus.BLOCKED);
        return new UserDTO(userRepository.save(user));
    }

    /* ── Unblock user ───────────────────────────────────────────────── */
    @Override
    @Transactional
    public UserDTO unblockUser(Long userId) {
        User user = findUserById(userId);
        user.setStatus(UserStatus.ACTIVE);
        return new UserDTO(userRepository.save(user));
    }

    /* ── Delete user (FIXED 🔥) ─────────────────────────────────────── */
    @Override
    @Transactional
    public void deleteUser(Long userId) {

        // ✅ check user exists
        User user = findUserById(userId);

        // 💥 STEP 1: delete child records (incomes)
        entityManager.createQuery("DELETE FROM Income i WHERE i.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // ✅ STEP 2: delete user
        userRepository.delete(user);
    }

    /* ── Private helper ─────────────────────────────────────────────── */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }
}