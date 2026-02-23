package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.IncomeDTO;
import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeService {

    Income createIncome(IncomeDTO dto, User user);

    List<Income> findByUser(User user);

    Optional<Income> findById(Long id);

    Income updateIncome(Long id, IncomeDTO dto, User user);

    void deleteById(Long id);

    List<Income> findByUserAndDateRange(User user, LocalDate start, LocalDate end);
}
