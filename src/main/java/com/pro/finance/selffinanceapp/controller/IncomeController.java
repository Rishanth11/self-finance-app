package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.IncomeDTO;
import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.service.IncomeService;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService userService; // implement method to find user by email/username

    // Create income
    @PostMapping
    public ResponseEntity<IncomeDTO> createIncome(@RequestBody IncomeDTO dto, Authentication authentication) {
        String username = authentication.getName(); // depends on your JWT principal
        User user = userService.findByEmail(username); // adapt if your principal is email/username
        Income income = Income.builder()
                .source(dto.getSource())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .user(user)
                .build();

        Income saved = incomeService.save(income);
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    // Get all incomes for current user
    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomes(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByEmail(username);
        List<Income> incomes = incomeService.findByUser(user);
        List<IncomeDTO> out = incomes.stream().map(i -> {
            IncomeDTO d = new IncomeDTO();
            d.setId(i.getId());
            d.setSource(i.getSource());
            d.setAmount(i.getAmount());
            d.setDate(i.getDate());
            return d;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByEmail(username);
        return incomeService.findById(id)
                .map(inc -> {
                    if (!inc.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(403).build();
                    }
                    incomeService.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
