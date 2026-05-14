package com.rishanth.flux360.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseDTO {

    private Long id;
    private String category;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
}