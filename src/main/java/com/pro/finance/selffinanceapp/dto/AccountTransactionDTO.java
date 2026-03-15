package com.pro.finance.selffinanceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTransactionDTO {
    private Long id;
    private BigDecimal amount;
    private String type;         // CREDIT, DEBIT, TRANSFER_IN, TRANSFER_OUT
    private String description;
    private LocalDate date;
    private Long accountId;
    private String accountName;
}