package com.pro.finance.selffinanceapp.dto;

public class FinancialHealthDTO {

    private double income;
    private double expense;
    private double savings;
    private double score;
    private String status;

    public FinancialHealthDTO(double income,
                              double expense,
                              double savings,
                              double score,
                              String status) {
        this.income = income;
        this.expense = expense;
        this.savings = savings;
        this.score = score;
        this.status = status;
    }

    public double getIncome() {
        return income;
    }

    public double getExpense() {
        return expense;
    }

    public double getSavings() {
        return savings;
    }

    public double getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }
}
