package com.pro.finance.selffinanceapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IncomeDTO {
    private Long id;
    private String source;
    private BigDecimal amount;
    private String category;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

}
