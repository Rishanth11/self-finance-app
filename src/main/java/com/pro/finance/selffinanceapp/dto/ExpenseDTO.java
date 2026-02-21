package com.pro.finance.selffinanceapp.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseDTO {

    private Long id;

    private Long userId;
    private String category;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
}