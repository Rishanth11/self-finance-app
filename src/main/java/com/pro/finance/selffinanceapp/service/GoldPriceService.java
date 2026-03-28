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
 * GoldPriceService — Updated to read ALL config from ApiConfigService (DB-backed).
 *
 * WHAT CHANGED FROM ORIGINAL:
 *   - goldApiKey        → apiConfig.get("GOLD_API_KEY")
 *   - primary URL       → apiConfig.get("GOLD_API_URL")
 *   - fallback URL      → apiConfig.get("GOLD_FALLBACK_URL")
 *   - cache TTL         → apiConfig.getInt("GOLD_CACHE_TTL_MINUTES", 15)
 *   - India correction  → apiConfig.getBigDecimal("GOLD_INDIA_CORRECTION", 1.0433)
 *   - USD/INR approx    → apiConfig.getBigDecimal("USD_TO_INR_APPROX", 84.50)
 *
 * Everything else (caching logic, fallback chain, timeouts) is unchanged.
 */
@Service
public class GoldPriceService {

    private final ApiConfigService apiConfig;

    // ── Manual in-memory cache ────────────────────────────────────────────────
    private volatile BigDecimal cachedPrice = null;
    private volatile Instant    cacheTime   = Instant.MIN;

    // ── RestTemplate with generous timeouts ───────────────────────────────────
    private final RestTemplate restTemplate;

    public GoldPriceService(ApiConfigService apiConfig) {
        this.apiConfig = apiConfig;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(12_000);
        factory.setReadTimeout(12_000);
        this.restTemplate = new RestTemplate(factory);
    }

    public BigDecimal getLiveGoldPricePerGram() {
        // ── Cache TTL from DB (default 15 min) ───────────────────────────────
        int ttlMinutes = apiConfig.getInt("GOLD_CACHE_TTL_MINUTES", 15);
        Duration cacheTtl = Duration.ofMinutes(ttlMinutes);

        if (cachedPrice != null &&
                Duration.between(cacheTime, Instant.now()).compareTo(cacheTtl) < 0) {
            System.out.println("✅ Gold price served from cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        BigDecimal result = fetchFromGoldApiInr();

        if (result == null) {
            System.out.println("⚠️ Primary goldapi.io failed, trying fallback...");
            result = fetchFallbackPrice();
        }

        if (result != null) {
            cachedPrice = result;
            cacheTime   = Instant.now();
            return result;
        }

        if (cachedPrice != null) {
            System.out.println("⚠️ All APIs failed — serving stale gold cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        System.out.println("❌ No cache and all APIs failed — returning hardcoded approximate");
        return new BigDecimal("14000.00");
    }

    private BigDecimal fetchFromGoldApiInr() {
        try {
            // ── URL and key from DB ───────────────────────────────────────────
            String url    = apiConfig.get("GOLD_API_URL", "https://www.goldapi.io/api/XAU/INR");
            String apiKey = apiConfig.get("GOLD_API_KEY");

            if (apiKey.isBlank()) {
                System.out.println("❌ GOLD_API_KEY is empty in DB and env — skipping primary fetch");
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ goldapi.io XAU/INR: unexpected response: " + body);
                return null;
            }

            BigDecimal priceInrPerOz = new BigDecimal(body.get("price").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            // ── Correction factor from DB ─────────────────────────────────────
            BigDecimal correctionFactor = apiConfig.getBigDecimal(
                    "GOLD_INDIA_CORRECTION", new BigDecimal("1.0433"));

            BigDecimal adjusted = priceInrPerOz
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(correctionFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("✅ Gold (goldapi.io): ₹" + priceInrPerOz + "/oz → ₹" + adjusted + "/g (factor: " + correctionFactor + ")");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ goldapi.io XAU/INR fetch failed: " + e.getMessage());
            return null;
        }
    }

    private BigDecimal fetchFallbackPrice() {
        try {
            String url = apiConfig.get("GOLD_FALLBACK_URL", "https://api.gold-api.com/price/XAU");
            Map body   = restTemplate.getForObject(url, Map.class);

            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ Fallback gold-api.com: unexpected response");
                return null;
            }

            BigDecimal priceUsdPerOz   = new BigDecimal(body.get("price").toString());
            BigDecimal usdToInr        = apiConfig.getBigDecimal("USD_TO_INR_APPROX", new BigDecimal("84.50"));
            BigDecimal gramsPerOz      = new BigDecimal("31.1035");
            BigDecimal correctionFactor = apiConfig.getBigDecimal("GOLD_INDIA_CORRECTION", new BigDecimal("1.0433"));

            BigDecimal adjusted = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(correctionFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("⚠️ Fallback gold (gold-api.com): ₹" + adjusted + "/g");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ Fallback gold-api.com also failed: " + e.getMessage());
            return null;
        }
    }

    public void evictCache() {
        cachedPrice = null;
        cacheTime   = Instant.MIN;
        System.out.println("🔄 Gold price cache evicted by admin");
    }
}