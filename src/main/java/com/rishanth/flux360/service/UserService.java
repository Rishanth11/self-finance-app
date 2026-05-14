package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.RegisterDTO;
import com.rishanth.flux360.model.User;

public interface UserService {
    User register(RegisterDTO dto);
    User findByEmail(String email);
}
