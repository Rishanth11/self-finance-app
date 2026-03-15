package com.pro.finance.selffinanceapp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransferRequestDTO {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
}