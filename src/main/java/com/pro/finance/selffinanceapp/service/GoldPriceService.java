package com.pro.finance.selffinanceapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class GoldPriceService {

    @Value("${twelvedata.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    @Cacheable(value = "goldPrice", unless = "#result == null")
    public BigDecimal getLiveGoldPricePerGram() {
        try {
            // Fetch XAU/USD gold price
            String goldUrl = "https://api.twelvedata.com/price?symbol=XAU/USD&apikey=" + apiKey;
            Map goldResponse = restTemplate.getForObject(goldUrl, Map.class);

            // Fetch live USD/INR forex rate
            String forexUrl = "https://api.twelvedata.com/price?symbol=USD/INR&apikey=" + apiKey;
            Map forexResponse = restTemplate.getForObject(forexUrl, Map.class);

            if (goldResponse == null || !goldResponse.containsKey("price")) {
                System.out.println("❌ Gold API invalid response: " + goldResponse);
                return null;
            }

            if (forexResponse == null || !forexResponse.containsKey("price")) {
                System.out.println("❌ Forex API invalid response: " + forexResponse);
                return null;
            }

            BigDecimal priceUsdPerOz = new BigDecimal(goldResponse.get("price").toString());
            BigDecimal usdToInr      = new BigDecimal(forexResponse.get("price").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal priceInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, RoundingMode.HALF_UP);

            System.out.println("✅ Gold price (INR/gram): " + priceInrPerGram
                    + " | USD/INR: " + usdToInr);

            return priceInrPerGram;

        } catch (Exception e) {
            System.out.println("❌ Gold API error: " + e.getMessage());
            return null;
        }
    }
}