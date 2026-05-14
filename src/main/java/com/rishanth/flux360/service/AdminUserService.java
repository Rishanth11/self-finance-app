package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.UserDTO;
import com.rishanth.flux360.dto.UserStatsDTO;
import java.util.List;

public interface AdminUserService {

    List<UserDTO>  getAllUsers();
    UserStatsDTO   getUserStats();
    UserDTO        blockUser(Long userId);
    UserDTO        unblockUser(Long userId);
    void           deleteUser(Long userId);
}