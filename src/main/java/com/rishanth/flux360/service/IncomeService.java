package com.rishanth.flux360.service;

import com.rishanth.flux360.dto.IncomeDTO;
import com.rishanth.flux360.model.Income;
import com.rishanth.flux360.model.User;

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
