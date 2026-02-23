package com.pro.finance.selffinanceapp.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GoldHistoryDTO {
    private Long id;
    private String purchaseDate;
    private double gramsPurchased;
    private double purchasePricePerGram;
    private double totalInvested;
    private double currentValue;
    private double profit;
}
