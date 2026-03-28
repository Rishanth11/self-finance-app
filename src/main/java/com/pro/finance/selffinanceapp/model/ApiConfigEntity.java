package com.pro.finance.selffinanceapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ApiConfigEntity — Stores API keys, URLs, and runtime config in DB.
 *
 * Admin can update these at runtime without redeploying.
 * GoldPriceService / SilverPriceService / NavService will read from here first,
 * then fall back to environment variables if DB value is missing.
 *
 * TABLE: api_config
 * KEY examples:
 *   GOLD_API_KEY               → goldapi.io access token
 *   TWELVEDATA_API_KEY         → twelvedata.com API key
 *   GOLD_API_URL               → https://www.goldapi.io/api/XAU/INR
 *   SILVER_API_URL             → https://www.goldapi.io/api/XAG/INR
 *   GOLD_FALLBACK_URL          → https://api.gold-api.com/price/XAU
 *   SILVER_FALLBACK_URL        → https://api.gold-api.com/price/XAG
 *   MFAPI_BASE_URL             → https://api.mfapi.in/mf
 *   GOLD_CACHE_TTL_MINUTES     → 15
 *   SILVER_CACHE_TTL_MINUTES   → 15
 *   GOLD_INDIA_CORRECTION      → 1.0433
 *   SILVER_INDIA_CORRECTION    → 1.0766
 *   USD_TO_INR_APPROX          → 84.50
 */
@Entity
@Table(name = "api_config")
public class ApiConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique config key — e.g. "GOLD_API_KEY", "MFAPI_BASE_URL"
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * Config value — stored as plain text.
     * For sensitive keys (API keys), admin should be careful.
     * Encrypt at rest if needed in future.
     */
    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    /**
     * Human-readable description shown in admin UI
     */
    @Column(name = "description", length = 300)
    private String description;

    /**
     * Category for grouping in admin UI: GOLD, SILVER, SIP, STOCKS, CACHE, CORRECTION
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * Whether this is a sensitive key (masked in UI)
     */
    @Column(name = "is_sensitive")
    private boolean sensitive = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────────────

    public ApiConfigEntity() {}

    public ApiConfigEntity(String configKey, String configValue,
                           String description, String category, boolean sensitive) {
        this.configKey   = configKey;
        this.configValue = configValue;
        this.description = description;
        this.category    = category;
        this.sensitive   = sensitive;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public String getConfigKey()                 { return configKey; }
    public void setConfigKey(String configKey)   { this.configKey = configKey; }

    public String getConfigValue()               { return configValue; }
    public void setConfigValue(String v)         { this.configValue = v; }

    public String getDescription()               { return description; }
    public void setDescription(String d)         { this.description = d; }

    public String getCategory()                  { return category; }
    public void setCategory(String c)            { this.category = c; }

    public boolean isSensitive()                 { return sensitive; }
    public void setSensitive(boolean s)          { this.sensitive = s; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)    { this.updatedAt = t; }

    public String getUpdatedBy()                 { return updatedBy; }
    public void setUpdatedBy(String u)           { this.updatedBy = u; }
}