package com.pro.finance.selffinanceapp.dto;

import com.pro.finance.selffinanceapp.model.Role;
import com.pro.finance.selffinanceapp.model.UserStatus;
import java.time.LocalDateTime;

public class UserDTO {

    private Long          id;
    private String        name;
    private String        email;
    private Role          role;
    private UserStatus    status;
    private LocalDateTime createdAt;

    // ── Constructor from entity ────────────────────────────────────────
    public UserDTO(com.pro.finance.selffinanceapp.model.User u) {
        this.id        = u.getId();
        this.name      = u.getName();
        this.email     = u.getEmail();
        this.role      = u.getRole();
        this.status    = u.getStatus();
        this.createdAt = u.getCreatedAt();
    }

    // ── Getters ────────────────────────────────────────────────────────
    public Long          getId()        { return id; }
    public String        getName()      { return name; }
    public String        getEmail()     { return email; }
    public Role          getRole()      { return role; }
    public UserStatus    getStatus()    { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}