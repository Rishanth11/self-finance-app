package com.pro.finance.selffinanceapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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

    public BigDecimal getLiveSilverPricePerGram() {
        try {
            String url = "https://api.twelvedata.com/price?symbol=XAG/USD&apikey=" + apiKey;

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("price")) {
                System.out.println("Silver API invalid response: " + response);
                return null;
            }

            BigDecimal priceUsdPerOz = new BigDecimal(response.get("price").toString());

            BigDecimal gramsPerOz = new BigDecimal("31.1035");
            BigDecimal usdToInr = new BigDecimal("83");

            BigDecimal priceInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, BigDecimal.ROUND_HALF_UP);

            return priceInrPerGram;

        } catch (Exception e) {
            System.out.println("Silver API error: " + e.getMessage());
            return null;
        }
    }
}