package com.pro.finance.selffinanceapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class NavService {

    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal fetchLatestNav(String fundCode) {

        try {
            String url = "https://api.mfapi.in/mf/" + fundCode;

            Map<String, Object> response =
                    restTemplate.getForObject(url, Map.class);

            List<Map<String, Object>> data =
                    (List<Map<String, Object>>) response.get("data");

            Object navValue = data.get(0).get("nav");

            return new BigDecimal(navValue.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch NAV");
        }
    }
}