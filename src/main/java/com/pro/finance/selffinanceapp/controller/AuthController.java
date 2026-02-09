package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.config.JwtUtil;
import com.pro.finance.selffinanceapp.dto.LoginDTO;
import com.pro.finance.selffinanceapp.dto.RegisterDTO;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    /* ---------------- REGISTER ---------------- */

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO dto) {
        try {
            User created = userService.register(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "id", created.getId(),
                            "email", created.getEmail()
                    ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /* ---------------- LOGIN ---------------- */

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getEmail(),
                            dto.getPassword()
                    )
            );

            // ✅ IMPORTANT: get full UserDetails (contains ROLE info)
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // ✅ Generate JWT WITH authorities
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(
                    Map.of(
                            "token", token,
                            "expiresIn", 24 * 60 * 60 // seconds
                    )
            );

        } catch (BadCredentialsException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
    }
}
