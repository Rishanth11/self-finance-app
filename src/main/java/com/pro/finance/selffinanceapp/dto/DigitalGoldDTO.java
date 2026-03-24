package com.pro.finance.selffinanceapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DigitalGoldDTO {

    @NotNull
    @Positive
    private BigDecimal gramsPurchased;

    @NotNull
    @Positive
    private BigDecimal purchasePricePerGram;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    public BigDecimal getGramsPurchased() {
        return gramsPurchased;
    }

    public void setGramsPurchased(BigDecimal gramsPurchased) {
        this.gramsPurchased = gramsPurchased;
    }

    public BigDecimal getPurchasePricePerGram() {
        return purchasePricePerGram;
    }

    public void setPurchasePricePerGram(BigDecimal purchasePricePerGram) {
        this.purchasePricePerGram = purchasePricePerGram;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}