package com.pro.finance.selffinanceapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoldPriceService {

    private final RestTemplate restTemplate = new RestTemplate();

    public double getLiveGoldPricePerGram() {

        // Example static fallback (replace with real API)
        double pricePerOunce = 1920; // USD
        return pricePerOunce / 31.1035;
    }
}

