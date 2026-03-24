package com.pro.finance.selffinanceapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class SilverPriceService {

    // ── Inject API key from application.properties / environment variable ─────
    // Local  → set in application-local.properties: goldapi.key=YOUR_KEY_HERE
    // Render → set in Environment Variables: GOLDAPI_KEY=YOUR_KEY_HERE
    @Value("${goldapi.key}")
    private String goldApiKey;

    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIMARY: goldapi.io — XAG/USD + frankfurter.app INR conversion
    //
    // goldapi.io free plan:
    //   • No credit card required
    //   • Requires API key via header: x-access-token
    //   • Endpoint: https://www.goldapi.io/api/XAG/USD
    //   • Returns price per troy ounce in USD
    //   • Sign up at: https://www.goldapi.io/
    //
    // Silver India correction factor (1.0766):
    //   • GST          → +3.0%
    //   • Customs duty → ~1.0%
    //   • MCX premium  → ~3.66% (silver premium is higher than gold)
    //
    // ⚠️ HOW TO RECALIBRATE if price drifts from Groww/MCX by more than ₹5/g:
    //   1. Note "Base INR/gram" from console log
    //   2. Check live price: groww.in/silver-rates
    //   3. New factor = Groww price ÷ Base INR/gram
    //   4. Update INDIA_CORRECTION_FACTOR below
    // ─────────────────────────────────────────────────────────────────────────
    @Cacheable(value = "silverPrice", unless = "#result == null")
    public BigDecimal getLiveSilverPricePerGram() {
        try {
            // ── Step 1: Get XAG/USD spot price from goldapi.io ────────────────────
            String silverUrl = "https://www.goldapi.io/api/XAG/USD";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-access-token", goldApiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    silverUrl, HttpMethod.GET, request, Map.class);

            Map silverResponse = response.getBody();

            if (silverResponse == null || !silverResponse.containsKey("price")) {
                System.out.println("❌ goldapi.io silver: invalid response: " + silverResponse);
                return fallbackXagPrice();
            }

            // ── Step 2: Get USD → INR exchange rate (free, no key) ────────────────
            String fxUrl = "https://api.frankfurter.app/latest?from=USD&to=INR";
            Map fxResponse = restTemplate.getForObject(fxUrl, Map.class);

            if (fxResponse == null || fxResponse.get("rates") == null) {
                System.out.println("❌ Frankfurter FX invalid response");
                return fallbackXagPrice();
            }

            Map rates = (Map) fxResponse.get("rates");
            if (!rates.containsKey("INR")) {
                System.out.println("❌ INR rate not found in FX response");
                return fallbackXagPrice();
            }

            // ── Step 3: Calculate base INR/gram ───────────────────────────────────
            // Formula: (USD/oz × USD→INR) ÷ 31.1035 g/oz
            BigDecimal priceUsdPerOz = new BigDecimal(silverResponse.get("price").toString());
            BigDecimal usdToInr      = new BigDecimal(rates.get("INR").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal baseInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, RoundingMode.HALF_UP);

            // ── Step 4: Apply India correction factor ─────────────────────────────
            BigDecimal INDIA_CORRECTION_FACTOR = new BigDecimal("1.0766");

            BigDecimal adjustedInrPerGram = baseInrPerGram
                    .multiply(INDIA_CORRECTION_FACTOR)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("✅ Silver price breakdown (goldapi.io):");
            System.out.println("   XAG/USD spot      : $" + priceUsdPerOz);
            System.out.println("   USD/INR rate      : ₹" + usdToInr);
            System.out.println("   Base INR/gram     : ₹" + baseInrPerGram + " (international spot)");
            System.out.println("   India adj(+7.66%) : ₹" + adjustedInrPerGram + " (GST + customs + MCX)");

            return adjustedInrPerGram;

        } catch (Exception e) {
            System.out.println("❌ goldapi.io silver fetch error: " + e.getMessage() + " — trying fallback");
            return fallbackXagPrice();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FALLBACK: gold-api.com (no key needed) — used only if goldapi.io fails
    // Less accurate on high-volatility days but always available.
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal fallbackXagPrice() {
        try {
            String fallbackUrl = "https://api.gold-api.com/price/XAG";
            Map silverResponse = restTemplate.getForObject(fallbackUrl, Map.class);

            if (silverResponse == null || !silverResponse.containsKey("price")) {
                System.out.println("❌ Fallback gold-api.com also failed");
                return null;
            }

            String fxUrl = "https://api.frankfurter.app/latest?from=USD&to=INR";
            Map fxResponse = restTemplate.getForObject(fxUrl, Map.class);

            if (fxResponse == null || fxResponse.get("rates") == null) return null;

            Map rates = (Map) fxResponse.get("rates");
            if (!rates.containsKey("INR")) return null;

            BigDecimal priceUsdPerOz = new BigDecimal(silverResponse.get("price").toString());
            BigDecimal usdToInr      = new BigDecimal(rates.get("INR").toString());
            BigDecimal gramsPerOz    = new BigDecimal("31.1035");

            BigDecimal baseInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, RoundingMode.HALF_UP);

            BigDecimal fallbackFactor = new BigDecimal("1.0766");
            BigDecimal adjusted = baseInrPerGram
                    .multiply(fallbackFactor)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("⚠️ Using fallback silver price (gold-api.com): ₹" + adjusted + "/g (approx)");
            return adjusted;

        } catch (Exception ex) {
            System.out.println("❌ Fallback gold-api.com also failed: " + ex.getMessage());
            return null;
        }
    }
}