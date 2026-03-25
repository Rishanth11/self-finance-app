package com.pro.finance.selffinanceapp.service;

import org.springframework.beans.factory.annotation.Value;
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
 * GoldPriceService — Fixed for production (Render deployment)
 *
 * ROOT CAUSES OF SLOW/FAILED GOLD PRICE IN PROD:
 *   1. @Cacheable requires a CacheManager bean. Without one configured,
 *      Spring silently skips caching — every request hits the API cold.
 *   2. Two sequential external HTTP calls (gold-api.com + frankfurter.app)
 *      = 6–10s on Render cold starts → "Backend returned no live price, falling
 *      back to direct fetch" warning you saw in the console.
 *   3. gold-api.com (free, no key) was being used even though you have a
 *      GOLD_API_KEY — goldapi.io supports XAU/INR natively, removing the
 *      need for a separate FX conversion call entirely.
 *
 * FIXES APPLIED:
 *   - Removed @Cacheable — replaced with manual volatile in-memory cache
 *     (no CacheManager or Caffeine dependency needed)
 *   - Switched to goldapi.io XAU/INR (same key as silver, single API call)
 *   - Removed frankfurter.app FX conversion call completely
 *   - Fallback uses gold-api.com XAU + hardcoded USD/INR (no second HTTP call)
 *   - Timeouts raised to 12s to handle Render's cold-start latency
 *   - Never returns null — always returns stale cache or a safe approximate
 *
 * CORRECTION FACTOR NOTE:
 *   The India correction factor (1.0433) is unchanged from your original code.
 *   It was verified correct on 23-Mar-2026 against Groww's ₹13,766/g price.
 *   If gold price drifts > ₹100 from Groww: new factor = Groww price ÷ base
 */
@Service
public class GoldPriceService {

    @Value("${goldapi.key}")
    private String goldApiKey;

    // ── Manual in-memory cache (avoids Spring CacheManager dependency) ────────
    private volatile BigDecimal cachedPrice = null;
    private volatile Instant    cacheTime   = Instant.MIN;
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    // ── RestTemplate with generous timeouts for cloud latency ─────────────────
    private final RestTemplate restTemplate;

    public GoldPriceService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(12_000);   // 12s — Render cold starts are slow
        factory.setReadTimeout(12_000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Returns live gold price per gram in INR.
     * Returns stale cached value if API fails (never returns null to callers).
     * Returns a hardcoded approximate only if no cache exists AND all APIs fail.
     */
    public BigDecimal getLiveGoldPricePerGram() {
        // ── Serve from cache if still fresh ───────────────────────────────────
        if (cachedPrice != null &&
                Duration.between(cacheTime, Instant.now()).compareTo(CACHE_TTL) < 0) {
            System.out.println("✅ Gold price served from cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        // ── PRIMARY: goldapi.io XAU/INR (single call — no FX conversion) ──────
        BigDecimal result = fetchFromGoldApiInr();

        // ── FALLBACK: gold-api.com XAU + hardcoded FX ─────────────────────────
        if (result == null) {
            System.out.println("⚠️ Primary goldapi.io failed, trying fallback...");
            result = fetchFallbackPrice();
        }

        // ── Update cache if we got a valid price ──────────────────────────────
        if (result != null) {
            cachedPrice = result;
            cacheTime   = Instant.now();
            return result;
        }

        // ── Last resort: return stale cache rather than null ──────────────────
        if (cachedPrice != null) {
            System.out.println("⚠️ All APIs failed — serving stale gold cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        // ── Absolute fallback: approximate MCX gold price (24K per gram) ──────
        // Prevents 500 errors on first deploy before any cache exists.
        // UPDATE this value if gold price moves significantly (check groww.in/gold-rates).
        System.out.println("❌ No cache and all APIs failed — returning hardcoded approximate gold price");
        return new BigDecimal("14000.00");   // approximate 24K gold per gram as of Mar 2026
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIMARY: goldapi.io — XAU/INR (direct INR, single API call)
    //
    // Using goldapi.io with your existing GOLD_API_KEY.
    // XAU/INR returns price per troy ounce in INR directly — no FX call needed.
    // Divide by 31.1035 g/oz to get per-gram price.
    //
    // India correction 1.0433 = GST(3%) + customs(~1%) + MCX premium(~0.33%)
    // Verified 23-Mar-2026: base ₹13,194/g × 1.0433 = ₹13,766/g (Groww: ₹13,766.50 ✅)
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal fetchFromGoldApiInr() {
        try {
            String url = "https://www.goldapi.io/api/XAU/INR";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", goldApiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> request  = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ goldapi.io XAU/INR: unexpected response: " + body);
                return null;
            }

            BigDecimal priceInrPerOz = new BigDecimal(body.get("price").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal baseInrPerGram = priceInrPerOz
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP);

            // India correction: GST(3%) + customs(~1%) + MCX premium(~0.33%)
            BigDecimal INDIA_CORRECTION_FACTOR = new BigDecimal("1.0433");

            BigDecimal adjustedInrPerGram = baseInrPerGram
                    .multiply(INDIA_CORRECTION_FACTOR)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("✅ Gold price breakdown (goldapi.io XAU/INR):");
            System.out.println("   XAU/INR per oz   : ₹" + priceInrPerOz);
            System.out.println("   Base per gram    : ₹" + baseInrPerGram);
            System.out.println("   India adj(+4.33%): ₹" + adjustedInrPerGram);

            return adjustedInrPerGram;

        } catch (Exception e) {
            System.out.println("❌ goldapi.io XAU/INR fetch failed: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FALLBACK: gold-api.com (free, no key) + hardcoded USD/INR
    //
    // frankfurter.app intentionally removed — it was the second serial HTTP
    // call causing 6–10s gateway timeouts on Render.
    // Hardcoded rate (84.50) is close enough for a fallback scenario.
    // Update USD_TO_INR_APPROX if the rupee moves significantly.
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal fetchFallbackPrice() {
        try {
            String url  = "https://api.gold-api.com/price/XAU";
            Map    body = restTemplate.getForObject(url, Map.class);

            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ Fallback gold-api.com: unexpected response");
                return null;
            }

            BigDecimal priceUsdPerOz = new BigDecimal(body.get("price").toString());

            // Hardcoded approximate — update periodically
            BigDecimal USD_TO_INR_APPROX   = new BigDecimal("84.50");
            BigDecimal gramsPerOz          = new BigDecimal("31.1035");
            BigDecimal INDIA_CORRECTION    = new BigDecimal("1.0433");

            BigDecimal adjusted = priceUsdPerOz
                    .multiply(USD_TO_INR_APPROX)
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(INDIA_CORRECTION)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("⚠️ Fallback gold price (gold-api.com): ₹" + adjusted + "/g");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ Fallback gold-api.com also failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Manually evict the cache — call from a @Scheduled job or admin endpoint
     * to force a refresh without relying on @Cacheable.
     */
    public void evictCache() {
        cachedPrice = null;
        cacheTime   = Instant.MIN;
        System.out.println("🔄 Gold price cache evicted");
    }
}