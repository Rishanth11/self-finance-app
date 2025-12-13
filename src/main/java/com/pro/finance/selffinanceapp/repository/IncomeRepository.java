package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserOrderByDateDesc(User user);
}
