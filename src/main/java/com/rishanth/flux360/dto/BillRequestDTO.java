package com.rishanth.flux360.dto;

import com.rishanth.flux360.model.BillCategory;
import com.rishanth.flux360.model.BillFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BillRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Bill name is required")
    private String billName;

    @NotNull(message = "Category is required")
    private BillCategory category;

    private String notes;

    @NotNull(message = "Frequency is required")
    private BillFrequency frequency;

    @Positive(message = "Amount must be greater than 0")
    private double amount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}