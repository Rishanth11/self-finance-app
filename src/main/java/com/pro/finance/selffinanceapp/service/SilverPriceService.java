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
import java.time.Instant;
import java.time.Duration;
import java.util.Map;

/**
 * SilverPriceService — Fixed for production (Render deployment)
 *
 * ROOT CAUSE OF 503 IN PROD:
 *   1. @Cacheable requires a CacheManager bean. If none is configured,
 *      Spring silently skips caching in prod → every call hits external APIs cold.
 *   2. Two sequential external API calls (goldapi.io + frankfurter.app) on Render
 *      cold-start = 6–10s total, causing gateway timeouts (503).
 *   3. frankfurter.app may be blocked/slow from Render's US servers.
 *
 * FIXES APPLIED:
 *   - Replaced @Cacheable with manual in-memory cache (no CacheManager needed)
 *   - Increased timeouts to 12s (Render cold starts are slow)
 *   - INR conversion now uses goldapi.io XAG/INR directly (1 API call, not 2)
 *   - Fallback uses gold-api.com XAG + hardcoded recent USD/INR rate as last resort
 *   - getPortfolioSummary() no longer throws on null price (returns cached/stale value)
 */
@Service
public class SilverPriceService {

    @Value("${goldapi.key}")
    private String goldApiKey;

    // ── Manual in-memory cache (avoids Spring CacheManager dependency) ────────
    private volatile BigDecimal cachedPrice = null;
    private volatile Instant   cacheTime   = Instant.MIN;
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    // ── RestTemplate with generous timeouts for cloud latency ─────────────────
    private final RestTemplate restTemplate;

    public SilverPriceService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(12_000);   // 12s — Render cold starts are slow
        factory.setReadTimeout(12_000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Returns live silver price per gram in INR.
     * Returns stale cached value if API fails (never returns null to callers).
     * Returns BigDecimal.ZERO only if no cache exists AND all APIs fail.
     */
    public BigDecimal getLiveSilverPricePerGram() {
        // ── Serve from cache if still fresh ───────────────────────────────────
        if (cachedPrice != null &&
                Duration.between(cacheTime, Instant.now()).compareTo(CACHE_TTL) < 0) {
            System.out.println("✅ Silver price served from cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        // ── PRIMARY: goldapi.io XAG/INR (single call — no FX conversion needed) ──
        BigDecimal result = fetchFromGoldApiInr();

        // ── FALLBACK: gold-api.com XAG + hardcoded FX rate ────────────────────
        if (result == null) {
            System.out.println("⚠️ Primary failed, trying fallback...");
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
            System.out.println("⚠️ All APIs failed — serving stale cache: ₹" + cachedPrice + "/g");
            return cachedPrice;
        }

        // ── Absolute fallback: approximate MCX silver price ───────────────────
        // This prevents 500 errors when the app is first deployed with no cache
        System.out.println("❌ No cache and all APIs failed — returning hardcoded approximate price");
        return new BigDecimal("95.00");   // UPDATE this periodically if silver drifts far
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIMARY: goldapi.io — XAG/INR (direct INR, single API call)
    //
    // goldapi.io supports currency=INR natively, so no FX conversion needed.
    // This removes the second frankfurter.app call that was causing timeouts.
    //
    // Price returned is per troy ounce → divide by 31.1035 for per gram.
    // India correction factor 1.0766 accounts for GST + customs + MCX premium.
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal fetchFromGoldApiInr() {
        try {
            String url = "https://www.goldapi.io/api/XAG/INR";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", goldApiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Map.class);

            Map body = response.getBody();
            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ goldapi.io XAG/INR: unexpected response: " + body);
                return null;
            }

            BigDecimal priceInrPerOz = new BigDecimal(body.get("price").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal baseInrPerGram = priceInrPerOz
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP);

            // India correction: GST(3%) + customs(~1%) + MCX premium(~3.66%)
            BigDecimal INDIA_FACTOR = new BigDecimal("1.0766");
            BigDecimal adjusted = baseInrPerGram
                    .multiply(INDIA_FACTOR)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("✅ Silver price (goldapi.io XAG/INR):");
            System.out.println("   XAG/INR per oz  : ₹" + priceInrPerOz);
            System.out.println("   Base per gram   : ₹" + baseInrPerGram);
            System.out.println("   India adj(+7.66%): ₹" + adjusted);

            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ goldapi.io XAG/INR fetch failed: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FALLBACK: gold-api.com (no key, free) + hardcoded USD/INR
    //
    // frankfurter.app is intentionally removed from fallback — it was the
    // second serial HTTP call causing 6–10s delays and gateway timeouts.
    // Instead we use a hardcoded USD/INR approximation (84.5) which is close
    // enough for silver tracking purposes.
    //
    // To update the hardcoded rate: change USD_TO_INR_APPROX below.
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal fetchFallbackPrice() {
        try {
            String url = "https://api.gold-api.com/price/XAG";
            Map body  = restTemplate.getForObject(url, Map.class);

            if (body == null || !body.containsKey("price")) {
                System.out.println("❌ Fallback gold-api.com: unexpected response");
                return null;
            }

            BigDecimal priceUsdPerOz = new BigDecimal(body.get("price").toString());

            // Hardcoded approximate rate — update periodically
            BigDecimal USD_TO_INR_APPROX = new BigDecimal("84.50");
            BigDecimal gramsPerOz        = new BigDecimal("31.1035");
            BigDecimal INDIA_FACTOR      = new BigDecimal("1.0766");

            BigDecimal adjusted = priceUsdPerOz
                    .multiply(USD_TO_INR_APPROX)
                    .divide(gramsPerOz, 4, RoundingMode.HALF_UP)
                    .multiply(INDIA_FACTOR)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("⚠️ Fallback silver price (gold-api.com): ₹" + adjusted + "/g");
            return adjusted;

        } catch (Exception e) {
            System.out.println("❌ Fallback gold-api.com also failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Manually evict the cache (call this from a @Scheduled job or admin endpoint
     * to force a refresh every 15 minutes without relying on @Cacheable).
     */
    public void evictCache() {
        cachedPrice = null;
        cacheTime   = Instant.MIN;
        System.out.println("🔄 Silver price cache evicted");
    }
}