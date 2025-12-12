package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.RegisterDTO;
import com.pro.finance.selffinanceapp.model.User;

public interface UserService {
    User register(RegisterDTO dto);
    User findByEmail(String email);
}
