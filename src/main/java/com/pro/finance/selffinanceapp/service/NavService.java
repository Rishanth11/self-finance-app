package com.pro.finance.selffinanceapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * NavService — Updated to read MFAPI base URL from ApiConfigService (DB-backed).
 *
 * WHAT CHANGED FROM ORIGINAL:
 *   - Hardcoded "https://api.mfapi.in/mf/" → apiConfig.get("MFAPI_BASE_URL")
 *   - Admin can change MFAPI URL at runtime if the endpoint changes
 */
@Service
public class NavService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ApiConfigService apiConfig;

    public NavService(ApiConfigService apiConfig) {
        this.apiConfig = apiConfig;
    }

    public BigDecimal fetchLatestNav(String fundCode) {
        try {
            String baseUrl = apiConfig.get("MFAPI_BASE_URL", "https://api.mfapi.in/mf");
            String url = baseUrl + "/" + fundCode;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            List<Map<String, Object>> data =
                    (List<Map<String, Object>>) response.get("data");

            Object navValue = data.get(0).get("nav");
            return new BigDecimal(navValue.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch NAV for fund code: " + fundCode + ". Error: " + e.getMessage());
        }
    }
}