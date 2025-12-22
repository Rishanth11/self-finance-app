package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.IncomeDTO;
import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.model.IncomeCategory;
import com.pro.finance.selffinanceapp.repository.IncomeRepository;
import com.pro.finance.selffinanceapp.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    @Override
    public Income createIncome(IncomeDTO dto, User user) {

        IncomeCategory category;
        try {
            category = IncomeCategory.valueOf(dto.getCategory().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid income category");
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
}

