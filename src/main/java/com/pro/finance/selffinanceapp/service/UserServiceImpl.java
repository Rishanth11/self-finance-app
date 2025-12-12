package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.RegisterDTO;
import com.pro.finance.selffinanceapp.model.Role;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User register(RegisterDTO dto) {
        if (repo.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // role: default to ROLE_USER unless admin specified
        if (dto.getRole() != null && dto.getRole().equalsIgnoreCase("admin")) {
            user.setRole(Role.ROLE_ADMIN);
        } else {
            user.setRole(Role.ROLE_USER);
        }
        return repo.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }
}
