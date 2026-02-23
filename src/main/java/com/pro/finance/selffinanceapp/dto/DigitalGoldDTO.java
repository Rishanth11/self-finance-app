package com.pro.finance.selffinanceapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class DigitalGoldDTO {

    private double gramsPurchased;
    private double purchasePricePerGram;
    private LocalDate purchaseDate;

    public double getGramsPurchased() {
        return gramsPurchased;
    }

    public void setGramsPurchased(double gramsPurchased) {
        this.gramsPurchased = gramsPurchased;
    }

    public double getPurchasePricePerGram() {
        return purchasePricePerGram;
    }

    public void setPurchasePricePerGram(double purchasePricePerGram) {
        this.purchasePricePerGram = purchasePricePerGram;
    }

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
