package com.pro.finance.selffinanceapp.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SilverPriceService {

    // ✅ No API key needed — Yahoo Finance is free with no key required
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
            // ── Primary: Yahoo Finance MCX Silver Futures ─────────────────────────
            //
            // WHY MCX instead of XAG/USD + correction factor?
            // Silver is highly volatile (can drop 6%+ in a single day). A fixed
            // correction factor drifts badly on high-volatility days. MCX gives
            // the actual Indian market price directly — no correction needed.
            //
            // SILVER.MCX = MCX Silver futures (1 KG contract, priced in INR/KG)
            // regularMarketPrice → INR per KG → divide by 1000 → INR per gram
            // This matches Groww/MCX prices exactly. ✅
            //
            String mcxUrl = "https://query1.finance.yahoo.com/v8/finance/chart/SILVER.MCX"
                    + "?interval=1m&range=1d";

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(mcxUrl, JsonNode.class);
            JsonNode meta = response.getBody()
                    .path("chart").path("result").get(0).path("meta");

            double pricePerKg = meta.path("regularMarketPrice").asDouble();

            if (pricePerKg <= 0) {
                System.out.println("❌ MCX Silver: invalid price from Yahoo Finance");
                return fallbackXagPrice();
            }

            // MCX silver is quoted per KG — convert to per gram
            BigDecimal pricePerGram = BigDecimal.valueOf(pricePerKg)
                    .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);

            System.out.println("✅ Silver price (MCX via Yahoo Finance):");
            System.out.println("   MCX price/kg  : ₹" + pricePerKg);
            System.out.println("   Price/gram     : ₹" + pricePerGram);

            return pricePerGram;

        } catch (Exception e) {
            System.out.println("❌ Yahoo Finance MCX error: " + e.getMessage() + " — trying fallback");
            return fallbackXagPrice();
        }
    }

    // ── Fallback: XAG/USD + frankfurter.app + correction ─────────────────────
    // Used only if Yahoo Finance MCX is unavailable.
    // Factor 1.0766 is an approximate mid-point India premium for silver.
    // May be slightly off on high-volatility days but better than nothing.
    private BigDecimal fallbackXagPrice() {
        try {
            String goldUrl = "https://api.gold-api.com/price/XAG";
            java.util.Map silverResponse = restTemplate.getForObject(goldUrl, java.util.Map.class);

            if (silverResponse == null || !silverResponse.containsKey("price")) {
                System.out.println("❌ Fallback XAG also failed");
                return null;
            }

            String fxUrl = "https://api.frankfurter.app/latest?from=USD&to=INR";
            java.util.Map fxResponse = restTemplate.getForObject(fxUrl, java.util.Map.class);

            if (fxResponse == null || fxResponse.get("rates") == null) return null;

            java.util.Map rates = (java.util.Map) fxResponse.get("rates");
            if (!rates.containsKey("INR")) return null;

            BigDecimal priceUsdPerOz = new BigDecimal(silverResponse.get("price").toString());
            BigDecimal usdToInr      = new BigDecimal(rates.get("INR").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal baseInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, RoundingMode.HALF_UP);

            // Approximate correction — less accurate than MCX but usable as fallback
            BigDecimal fallbackFactor = new BigDecimal("1.0766");
            BigDecimal adjusted = baseInrPerGram
                    .multiply(fallbackFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("⚠️ Using fallback XAG price: ₹" + adjusted + "/g (approx)");
            return adjusted;

        } catch (Exception ex) {
            System.out.println("❌ Fallback XAG also failed: " + ex.getMessage());
            return null;
        }
    }
}