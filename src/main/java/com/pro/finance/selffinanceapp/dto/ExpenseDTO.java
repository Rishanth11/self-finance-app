package com.pro.finance.selffinanceapp.dto;

import java.time.LocalDate;

public class ExpenseDTO {

    private Long userId;
    private String category;
    private double amount;
    private LocalDate expenseDate;
    private String description;

    // Default constructor
    public ExpenseDTO() {
    }

    // Parameterized constructor
    public ExpenseDTO(Long userId, String category, double amount, LocalDate expenseDate, String description) {
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
