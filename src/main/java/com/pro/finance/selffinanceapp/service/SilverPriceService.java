package com.pro.finance.selffinanceapp.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class SilverPriceService {

    @Value("${twelvedata.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    @Cacheable(value = "silverPrice", unless = "#result == null")
    public BigDecimal getLiveSilverPricePerGram() {
        try {
            // ✅ Free API - no key required, no rate limits
            String silverUrl = "https://api.gold-api.com/price/XAG";
            Map silverResponse = restTemplate.getForObject(silverUrl, Map.class);

            if (silverResponse == null || !silverResponse.containsKey("price")) {
                System.out.println("❌ Silver API invalid response: " + silverResponse);
                return null;
            }

            // gold-api.com returns price per troy ounce in USD
            BigDecimal priceUsdPerOz = new BigDecimal(silverResponse.get("price").toString());

            // Fetch live USD/INR from Twelve Data
            String forexUrl = "https://api.twelvedata.com/price?symbol=USD/INR&apikey=" + apiKey;
            Map forexResponse = restTemplate.getForObject(forexUrl, Map.class);

            if (forexResponse == null || !forexResponse.containsKey("price")) {
                System.out.println("❌ Forex API invalid response: " + forexResponse);
                return null;
            }

            BigDecimal usdToInr   = new BigDecimal(forexResponse.get("price").toString());
            BigDecimal gramsPerOz = new BigDecimal("31.1035");

            BigDecimal priceInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, RoundingMode.HALF_UP);

            System.out.println("✅ Silver price (INR/gram): " + priceInrPerGram
                    + " | USD/INR: " + usdToInr);

            return priceInrPerGram;

        } catch (Exception e) {
            System.out.println("❌ Silver API error: " + e.getMessage());
            return null;
        }
    }
}