package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Role;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // ── Admin queries ──────────────────────────────────────────────────
    long countByStatus(UserStatus status);
    long countByRole(Role role);
    List<User> findByStatus(UserStatus status);
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}