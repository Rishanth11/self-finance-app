package com.pro.finance.selffinanceapp.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class GoldPriceService {

    private final RestTemplate restTemplate = createRestTemplate();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    @Cacheable(value = "goldPrice", unless = "#result == null")
    public BigDecimal getLiveGoldPricePerGram() {
        try {
            // ── Step 1: Get XAU/USD spot price (free, no key, real-time) ──────────
            String goldUrl = "https://api.gold-api.com/price/XAU";
            Map goldResponse = restTemplate.getForObject(goldUrl, Map.class);

            if (goldResponse == null || !goldResponse.containsKey("price")) {
                System.out.println("❌ gold-api.com invalid response: " + goldResponse);
                return null;
            }

            // ── Step 2: Get USD → INR exchange rate (free, no key, real-time) ─────
            String fxUrl = "https://api.frankfurter.app/latest?from=USD&to=INR";
            Map fxResponse = restTemplate.getForObject(fxUrl, Map.class);

            if (fxResponse == null || fxResponse.get("rates") == null) {
                System.out.println("❌ Frankfurter FX invalid response: " + fxResponse);
                return null;
            }

            Map rates = (Map) fxResponse.get("rates");
            if (!rates.containsKey("INR")) {
                System.out.println("❌ INR rate not found in FX response");
                return null;
            }

            // ── Step 3: Calculate base INR/gram from international spot price ──────
            // Formula: (USD/oz × USD→INR) ÷ 31.1035 g/oz
            BigDecimal priceUsdPerOz  = new BigDecimal(goldResponse.get("price").toString());
            BigDecimal usdToInr       = new BigDecimal(rates.get("INR").toString());
            BigDecimal gramsPerOz     = new BigDecimal("31.1035");

            BigDecimal baseInrPerGram = priceUsdPerOz
                    .multiply(usdToInr)
                    .divide(gramsPerOz, 2, RoundingMode.HALF_UP);

            // ── Step 4: Apply India correction factor ─────────────────────────────
            //
            // International spot price (XAU/USD converted) is always lower than
            // Indian retail gold price (Groww, MCX, jewellers) because India adds:
            //   • GST          → +3.0%
            //   • Customs duty → ~1.0% (net absorbed portion)
            //   • MCX premium  → ~0.33%
            //
            // Factor verified on 23-Mar-2026:
            //   Base = ₹13,194/g  →  ×1.0433  →  ₹13,766/g  ✅  (Groww: ₹13,766.50)
            //
            // ⚠️ HOW TO RECALIBRATE if your price drifts from Groww by more than ₹100:
            //   1. Note today's base from console log: "Base INR/gram"
            //   2. Check Groww: groww.in/gold-rates
            //   3. New factor = Groww price ÷ Base INR/gram
            //   4. Update INDIA_CORRECTION_FACTOR here AND in digital-gold.html
            //
            BigDecimal INDIA_CORRECTION_FACTOR = new BigDecimal("1.0433");

            BigDecimal adjustedInrPerGram = baseInrPerGram
                    .multiply(INDIA_CORRECTION_FACTOR)
                    .setScale(2, RoundingMode.HALF_UP);

            System.out.println("✅ Gold price breakdown:");
            System.out.println("   XAU/USD spot      : $"  + priceUsdPerOz);
            System.out.println("   USD/INR rate      : ₹"  + usdToInr);
            System.out.println("   Base INR/gram     : ₹"  + baseInrPerGram + " (international spot)");
            System.out.println("   India adj(+4.33%) : ₹"  + adjustedInrPerGram + " (GST + customs + MCX)");

            return adjustedInrPerGram;

        } catch (Exception e) {
            System.out.println("❌ Gold price fetch error: " + e.getMessage());
            return null;
        }
    }
}