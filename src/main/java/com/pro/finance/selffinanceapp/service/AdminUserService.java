package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.UserDTO;
import com.pro.finance.selffinanceapp.dto.UserStatsDTO;
import java.util.List;

public interface AdminUserService {

    List<UserDTO>  getAllUsers();
    UserStatsDTO   getUserStats();
    UserDTO        blockUser(Long userId);
    UserDTO        unblockUser(Long userId);
    void           deleteUser(Long userId);
}