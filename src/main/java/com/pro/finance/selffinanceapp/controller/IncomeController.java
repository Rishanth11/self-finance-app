package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.IncomeDTO;
import com.pro.finance.selffinanceapp.model.Income;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.IncomeService;
import com.pro.finance.selffinanceapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService userService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> createIncome(
            @RequestBody IncomeDTO dto,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();
            User user = userService.findByEmail(username);

            Income saved = incomeService.createIncome(dto, user);
            dto.setId(saved.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add income");
        }
    }

    // READ
    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomes(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByEmail(username);

        List<IncomeDTO> out = incomeService.findByUser(user)
                .stream()
                .map(i -> {
                    IncomeDTO d = new IncomeDTO();
                    d.setId(i.getId());
                    d.setSource(i.getSource());
                    d.setAmount(i.getAmount());
                    d.setDate(i.getDate());
                    d.setCategory(i.getCategory().name());
                    d.setDescription(i.getDescription());
                    return d;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncome(
            @PathVariable Long id,
            @RequestBody IncomeDTO dto,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();
            User user = userService.findByEmail(username);

            Income updated = incomeService.updateIncome(id, dto, user);
            dto.setId(updated.getId());

            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncome(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userService.findByEmail(username);

        return incomeService.findById(id)
                .map(inc -> {
                    if (!inc.getUser().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    incomeService.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<IncomeDTO>> getIncomesByMonth(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userService.findByEmail(username);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<IncomeDTO> out = incomeService
                .findByUserAndDateRange(user, start, end)
                .stream()
                .map(i -> {
                    IncomeDTO d = new IncomeDTO();
                    d.setId(i.getId());
                    d.setSource(i.getSource());
                    d.setAmount(i.getAmount());
                    d.setDate(i.getDate());
                    d.setCategory(i.getCategory().name());
                    d.setDescription(i.getDescription());
                    return d;
                })
                .toList();

        return ResponseEntity.ok(out);
    }
}
