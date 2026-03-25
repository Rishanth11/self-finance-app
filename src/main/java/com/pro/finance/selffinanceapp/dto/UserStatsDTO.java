package com.pro.finance.selffinanceapp.dto;

public class UserStatsDTO {

    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long adminUsers;

    public UserStatsDTO(long totalUsers, long activeUsers, long blockedUsers, long adminUsers) {
        this.totalUsers   = totalUsers;
        this.activeUsers  = activeUsers;
        this.blockedUsers = blockedUsers;
        this.adminUsers   = adminUsers;
    }

    public long getTotalUsers()   { return totalUsers; }
    public long getActiveUsers()  { return activeUsers; }
    public long getBlockedUsers() { return blockedUsers; }
    public long getAdminUsers()   { return adminUsers; }
}