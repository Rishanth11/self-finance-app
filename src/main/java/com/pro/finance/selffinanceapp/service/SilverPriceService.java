package com.pro.finance.selffinanceapp.service;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class SilverPriceService {

    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 5 seconds
        factory.setReadTimeout(5000);     // 5 seconds
        return new RestTemplate(factory);
    }

    public BigDecimal getLiveSilverPricePerGram() {

        try {

            // Step 1: Get silver price in USD per troy ounce
            String silverUrl = "https://api.gold-api.com/price/XAG";

            Map silverResponse = restTemplate.getForObject(silverUrl, Map.class);

            if (silverResponse == null || !silverResponse.containsKey("price")) {
                System.out.println("Silver API returned empty response");
                return null;
            }

            BigDecimal priceUsdPerOz = new BigDecimal(silverResponse.get("price").toString());
            System.out.println("Silver price (USD/oz): " + priceUsdPerOz);


            // Step 2: Get USD → INR rate
            String fxUrl = "https://api.frankfurter.app/latest?from=USD&to=INR";

            Map fxResponse = restTemplate.getForObject(fxUrl, Map.class);

            if (fxResponse == null || !fxResponse.containsKey("rates")) {
                System.out.println("FX API returned empty response");
                return null;
            }

            Map<String, Object> rates = (Map<String, Object>) fxResponse.get("rates");

            BigDecimal usdToInr = new BigDecimal(rates.get("INR").toString());

            System.out.println("USD to INR rate: " + usdToInr);


            // Step 3: Convert ounce → gram
            BigDecimal gramsPerOz = new BigDecimal("31.1035");

            BigDecimal priceInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, RoundingMode.HALF_UP);

            System.out.println("Live silver price (INR/gram): " + priceInrPerGram);

            return priceInrPerGram;

        } catch (Exception e) {

            System.out.println("Error fetching silver price: " + e.getMessage());
            e.printStackTrace();
            return null;

        }
    }
}