package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.model.User;

import java.util.List;
import java.util.Optional;

public interface IncomeService {
    Income save(Income income);
    List<Income> findByUser(User user);
    Optional<Income> findById(Long id);
    void deleteById(Long id);
}
