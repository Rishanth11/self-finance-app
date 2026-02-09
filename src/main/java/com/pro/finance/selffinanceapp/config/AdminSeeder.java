package com.pro.finance.selffinanceapp.config;

import com.pro.finance.selffinanceapp.model.Role;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AdminSeeder {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void createAdminIfNotExists() {

        String adminEmail = "admin@gmail.com";

        if (!repo.existsByEmail(adminEmail)) {

            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);

            repo.save(admin);

            System.out.println("✅ Default ADMIN user created");
        } else {
            System.out.println("ℹ️ ADMIN user already exists");
        }
    }
}
