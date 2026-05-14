package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.Role;
import com.rishanth.flux360.model.User;
import com.rishanth.flux360.model.UserStatus;
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