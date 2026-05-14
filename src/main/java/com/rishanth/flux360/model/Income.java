package com.rishanth.flux360.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "incomes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private IncomeCategory category;

    private String source;

    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
