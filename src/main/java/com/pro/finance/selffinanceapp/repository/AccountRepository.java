package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Account;
import com.pro.finance.selffinanceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    List<Account> findByUserAndType(User user, Account.AccountType type);
}