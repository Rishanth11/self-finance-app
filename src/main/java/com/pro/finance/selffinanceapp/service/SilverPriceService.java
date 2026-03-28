package com.pro.finance.selffinanceapp.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * SilverPriceService — Updated to read ALL config from ApiConfigService (DB-backed).
 *
 * WHAT CHANGED FROM ORIGINAL:
 *   - goldApiKey        → apiConfig.get("GOLD_API_KEY")   (same key used for silver)
 *   - primary URL       → apiConfig.get("SILVER_API_URL")
 *   - fallback URL      → apiConfig.get("SILVER_FALLBACK_URL")
 *   - cache TTL         → apiConfig.getInt("SILVER_CACHE_TTL_MINUTES", 15)
 *   - India correction  → apiConfig.getBigDecimal("SILVER_INDIA_CORRECTION", 1.0766)
 *   - USD/INR approx    → apiConfig.getBigDecimal("USD_TO_INR_APPROX", 84.50)
 */
@Service
public class SilverPriceService {

    private final ApiConfigService apiConfig;

    // ── Manual in-memory cache ────────────────────────────────────────────────
    private volatile BigDecimal cachedPrice = null;
    private volatile Instant   cacheTime   = Instant.MIN;

    // ── RestTemplate with generous timeouts ───────────────────────────────────
    private final RestTemplate restTemplate;

    public SilverPriceService(ApiConfigService apiConfig) {
        this.apiConfig = apiConfig;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(12_000);
        factory.setReadTimeout(12_000);
        this.restTemplate = new RestTemplate(factory);
    }

    public BigDecimal getLiveSilverPricePerGram() {
        // ── Cache TTL from DB (default 15 min) ───────────────────────────────
        int ttlMinutes = apiConfig.getInt("SILVER_CACHE_TTL_MINUTES", 15);
        Duration cacheTtl = Duration.ofMinutes(ttlMinutes);

        if (cachedPrice != null &&
                Duration.between(cacheTime, Instant.now()).compareTo(cacheTtl) < 0) {
            System.out.println("✅ Silver price served from cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        BigDecimal result = fetchFromGoldApiInr();

        if (result == null) {
            System.out.println("⚠️ Primary failed, trying fallback...");
            result = fetchFallbackPrice();
        }

        if (result != null) {
            cachedPrice = result;
            cacheTime   = Instant.now();
            return result;
        }

        if (cachedPrice != null) {
            System.out.println("⚠️ All APIs failed — serving stale silver cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        System.out.println("❌ No cache and all APIs failed — returning hardcoded approximate");
        return new BigDecimal("95.00");
    }

    private BigDecimal fetchFromGoldApiInr() {
        try {
            String url    = apiConfig.get("SILVER_API_URL", "https://www.goldapi.io/api/XAG/INR");
            String apiKey = apiConfig.get("GOLD_API_KEY");   // same key covers XAG too

            if (apiKey.isBlank()) {
                System.out.println("❌ GOLD_API_KEY (used for silver) is empty — skipping primary fetch");
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ goldapi.io XAG/INR: unexpected response: " + body);
                return null;
            }

            BigDecimal priceInrPerOz = new BigDecimal(body.get("price").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal correctionFactor = apiConfig.getBigDecimal(
                    "SILVER_INDIA_CORRECTION", new BigDecimal("1.0766"));

            BigDecimal adjusted = priceInrPerOz
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(correctionFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("✅ Silver (goldapi.io): ₹" + priceInrPerOz + "/oz → ₹" + adjusted + "/g (factor: " + correctionFactor + ")");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ goldapi.io XAG/INR fetch failed: " + e.getMessage());
            return null;
        }
    }

    private BigDecimal fetchFallbackPrice() {
        try {
            String url = apiConfig.get("SILVER_FALLBACK_URL", "https://api.gold-api.com/price/XAG");
            Map body   = restTemplate.getForObject(url, Map.class);

            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ Fallback gold-api.com (silver): unexpected response");
                return null;
            }

            BigDecimal priceUsdPerOz    = new BigDecimal(body.get("price").toString());
            BigDecimal usdToInr         = apiConfig.getBigDecimal("USD_TO_INR_APPROX", new BigDecimal("84.50"));
            BigDecimal gramsPerOz       = new BigDecimal("31.1035");
            BigDecimal correctionFactor = apiConfig.getBigDecimal("SILVER_INDIA_CORRECTION", new BigDecimal("1.0766"));

            BigDecimal adjusted = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(correctionFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("⚠️ Fallback silver (gold-api.com): ₹" + adjusted + "/g");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ Fallback silver (gold-api.com) failed: " + e.getMessage());
            return null;
        }
    }

    public void evictCache() {
        cachedPrice = null;
        cacheTime   = Instant.MIN;
        System.out.println("🔄 Silver price cache evicted by admin");
    }
}