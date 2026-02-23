package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.IncomeDTO;
import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.model.IncomeCategory;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    @Override
    public Income createIncome(IncomeDTO dto, User user) {

        if (dto.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        if (dto.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }

        if (dto.getSource() == null || dto.getSource().isBlank()) {
            throw new IllegalArgumentException("Source is required");
        }

        IncomeCategory category;
        try {
            category = IncomeCategory.valueOf(dto.getCategory().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid income category");
        }

        Income income = Income.builder()
                .amount(dto.getAmount())
                .date(dto.getDate())
                .source(dto.getSource())
                .description(dto.getDescription())
                .category(category)
                .user(user)
                .build();

        return incomeRepository.save(income);
    }

    @Override
    public Income updateIncome(Long id, IncomeDTO dto, User user) {

        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        if (!income.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        income.setSource(dto.getSource());
        income.setAmount(dto.getAmount());
        income.setDate(dto.getDate());
        income.setDescription(dto.getDescription());

        if (dto.getCategory() != null) {
            try {
                income.setCategory(
                        IncomeCategory.valueOf(dto.getCategory().toUpperCase())
                );
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid income category");
            }
        }

        return incomeRepository.save(income);
    }

    @Override
    public List<Income> findByUser(User user) {
        return incomeRepository.findByUserOrderByDateDesc(user);
    }

    @Override
    public Optional<Income> findById(Long id) {
        return incomeRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        incomeRepository.deleteById(id);
    }

    @Override
    public List<Income> findByUserAndDateRange(User user,
                                               LocalDate start,
                                               LocalDate end) {
        return incomeRepository
                .findByUserAndDateBetweenOrderByDateDesc(user, start, end);
    }
}
