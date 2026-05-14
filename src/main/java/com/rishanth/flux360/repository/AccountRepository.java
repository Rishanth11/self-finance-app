package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.Account;
import com.rishanth.flux360.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    List<Account> findByUserAndType(User user, Account.AccountType type);
}