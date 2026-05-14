package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.RegisterDTO;
import com.rishanth.flux360.model.Role;
import com.rishanth.flux360.model.User;
import com.rishanth.flux360.repository.UserRepository;
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

        user.setRole(Role.ROLE_USER);

        return repo.save(user);
    }


    @Override
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }
}
