package com.pro.finance.selffinanceapp.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoldPriceService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String API_KEY = "goldapi-147028smly4r6i0-io";

    public double getLiveGoldPricePerGram() {

        try {

            String url = "https://www.goldapi.io/api/XAU/INR";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", API_KEY);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            Map.class
                    );

            Object priceObj = response.getBody().get("price");

            double pricePerOunce = Double.parseDouble(priceObj.toString());

            return pricePerOunce / 31.1035; // ounce → gram

        } catch (Exception e) {

            // fallback value if API fails
            return 6200.0;

        }
    }
}