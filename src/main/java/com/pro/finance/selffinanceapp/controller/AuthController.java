package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.LoginDTO;
import com.pro.finance.selffinanceapp.dto.RegisterDTO;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.UserService;
import com.pro.finance.selffinanceapp.config.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO dto) {
        try {
            User created = userService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("id", created.getId(), "email", created.getEmail()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            UserDetails ud = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(ud.getUsername());
            return ResponseEntity.ok(Map.of("token", token, "expiresIn", 24 * 3600));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }
}