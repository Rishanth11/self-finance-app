package com.pro.finance.selffinanceapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class NavService {

    private final RestTemplate restTemplate = new RestTemplate();

    public double fetchLatestNav(String fundCode) {
        String url = "https://api.mfapi.in/mf/" + fundCode;

        Map response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> data = (List<Map<String, String>>) response.get("data");

        return Double.parseDouble(data.get(0).get("nav"));
    }
}
