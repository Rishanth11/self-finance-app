package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.ApiConfigEntity;
import com.pro.finance.selffinanceapp.repository.ApiConfigRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ApiConfigService — Single source of truth for all API keys, URLs, and settings.
 *
 * PRIORITY ORDER:
 *   1. DB value (admin-managed, runtime-changeable)
 *   2. Environment variable / application.properties value (safe fallback)
 *   3. Hardcoded default (last resort, never null)
 *
 * HOW TO USE IN OTHER SERVICES:
 *   @Autowired ApiConfigService apiConfig;
 *   String key = apiConfig.get("GOLD_API_KEY");
 *   String url = apiConfig.get("GOLD_API_URL");
 */
@Service
public class ApiConfigService {

    private final ApiConfigRepository repo;

    // ── Env var / properties fallbacks ───────────────────────────────────────
    @Value("${goldapi.key:}")
    private String envGoldApiKey;

    @Value("${twelvedata.api.key:}")
    private String envTwelvedataKey;

    public ApiConfigService(ApiConfigRepository repo) {
        this.repo = repo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEED DEFAULTS on first startup
    // If a key doesn't exist in DB yet, insert it with env/default value.
    // This runs once on startup — safe to re-run (uses existsByConfigKey check).
    // ─────────────────────────────────────────────────────────────────────────
    @PostConstruct
    public void seedDefaults() {
        seed("GOLD_API_KEY",
                envGoldApiKey.isBlank() ? "" : envGoldApiKey,
                "goldapi.io access token for Gold & Silver prices",
                "KEYS", true);

        seed("TWELVEDATA_API_KEY",
                envTwelvedataKey.isBlank() ? "" : envTwelvedataKey,
                "Twelvedata.com API key for live stock prices",
                "KEYS", true);

        seed("GOLD_API_URL",
                "https://www.goldapi.io/api/XAU/INR",
                "Primary Gold price endpoint (XAU/INR)",
                "GOLD", false);

        seed("GOLD_FALLBACK_URL",
                "https://api.gold-api.com/price/XAU",
                "Fallback Gold price endpoint (USD, no key needed)",
                "GOLD", false);

        seed("SILVER_API_URL",
                "https://www.goldapi.io/api/XAG/INR",
                "Primary Silver price endpoint (XAG/INR)",
                "SILVER", false);

        seed("SILVER_FALLBACK_URL",
                "https://api.gold-api.com/price/XAG",
                "Fallback Silver price endpoint (USD, no key needed)",
                "SILVER", false);

        seed("MFAPI_BASE_URL",
                "https://api.mfapi.in/mf",
                "MFAPI.in base URL for SIP NAV fetch",
                "SIP", false);

        seed("MFAPI_SEARCH_URL",
                "https://api.mfapi.in/mf/search",
                "MFAPI.in search endpoint for fund scheme lookup",
                "SIP", false);

        seed("GOLD_CACHE_TTL_MINUTES",
                "15",
                "Gold price cache TTL in minutes",
                "CACHE", false);

        seed("SILVER_CACHE_TTL_MINUTES",
                "15",
                "Silver price cache TTL in minutes",
                "CACHE", false);

        seed("GOLD_INDIA_CORRECTION",
                "1.0433",
                "India correction factor for gold (GST 3% + customs ~1% + MCX ~0.33%)",
                "CORRECTION", false);

        seed("SILVER_INDIA_CORRECTION",
                "1.0766",
                "India correction factor for silver (GST 3% + customs ~1% + MCX ~3.66%)",
                "CORRECTION", false);

        seed("USD_TO_INR_APPROX",
                "84.50",
                "Approximate USD to INR rate used in fallback price calculation",
                "CORRECTION", false);

        System.out.println("✅ ApiConfigService: defaults seeded into DB");
    }

    private void seed(String key, String value, String desc, String category, boolean sensitive) {
        if (!repo.existsByConfigKey(key)) {
            repo.save(new ApiConfigEntity(key, value, desc, category, sensitive));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET — DB first, then env var fallback, then hardcoded default
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get config value by key.
     * Returns empty string (never null) if nothing found.
     */
    public String get(String key) {
        Optional<ApiConfigEntity> opt = repo.findByConfigKey(key);
        if (opt.isPresent() && !opt.get().getConfigValue().isBlank()) {
            return opt.get().getConfigValue().trim();
        }
        // Fallback to env vars for sensitive keys
        return envFallback(key);
    }

    /**
     * Get config value with a hardcoded default if nothing found.
     */
    public String get(String key, String defaultValue) {
        String val = get(key);
        return val.isBlank() ? defaultValue : val;
    }

    /**
     * Get config value as BigDecimal (for correction factors, etc.)
     */
    public java.math.BigDecimal getBigDecimal(String key, java.math.BigDecimal defaultValue) {
        try {
            String val = get(key);
            return val.isBlank() ? defaultValue : new java.math.BigDecimal(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get config value as int (for TTL minutes, etc.)
     */
    public int getInt(String key, int defaultValue) {
        try {
            String val = get(key);
            return val.isBlank() ? defaultValue : Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String envFallback(String key) {
        return switch (key) {
            case "GOLD_API_KEY"       -> envGoldApiKey;
            case "TWELVEDATA_API_KEY" -> envTwelvedataKey;
            default -> "";
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN CRUD
    // ─────────────────────────────────────────────────────────────────────────

    public List<ApiConfigEntity> getAll() {
        return repo.findAll();
    }

    public List<ApiConfigEntity> getByCategory(String category) {
        return repo.findByCategory(category);
    }

    /**
     * Update a config value. Creates it if it doesn't exist.
     */
    public ApiConfigEntity update(String key, String value, String updatedBy) {
        ApiConfigEntity entity = repo.findByConfigKey(key)
                .orElse(new ApiConfigEntity(key, value, "", "CUSTOM", false));
        entity.setConfigValue(value.trim());
        entity.setUpdatedBy(updatedBy);
        ApiConfigEntity saved = repo.save(entity);
        System.out.println("🔧 ApiConfig updated: " + key + " by " + updatedBy);
        return saved;
    }

    /**
     * Bulk update multiple keys at once (used by admin page Save All button).
     */
    public void updateBulk(Map<String, String> updates, String updatedBy) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            update(entry.getKey(), entry.getValue(), updatedBy);
        }
    }

    public ApiConfigEntity getEntityByKey(String key) {
        return repo.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config key not found: " + key));
    }
}