package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.model.ApiConfigEntity;
import com.pro.finance.selffinanceapp.service.ApiConfigService;
import com.pro.finance.selffinanceapp.service.GoldPriceService;
import com.pro.finance.selffinanceapp.service.SilverPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * AdminApiConfigController — REST API for admin to manage API keys, URLs, settings.
 *
 * ALL endpoints require ROLE_ADMIN.
 *
 * Endpoints:
 *   GET    /api/admin/config              → Get all config entries
 *   GET    /api/admin/config/{key}        → Get single config entry
 *   PUT    /api/admin/config/{key}        → Update single config value
 *   POST   /api/admin/config/bulk         → Bulk update multiple keys
 *   POST   /api/admin/config/cache/evict  → Force evict gold+silver cache
 */
@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiConfigController {

    private final ApiConfigService configService;
    private final GoldPriceService goldPriceService;
    private final SilverPriceService silverPriceService;

    public AdminApiConfigController(ApiConfigService configService,
                                    GoldPriceService goldPriceService,
                                    SilverPriceService silverPriceService) {
        this.configService     = configService;
        this.goldPriceService  = goldPriceService;
        this.silverPriceService = silverPriceService;
    }

    // ── GET ALL ──────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<ApiConfigEntity>> getAll() {
        List<ApiConfigEntity> all = configService.getAll();

        // Mask sensitive values before sending to frontend
        all.forEach(e -> {
            if (e.isSensitive() && !e.getConfigValue().isBlank()) {
                e.setConfigValue(maskKey(e.getConfigValue()));
            }
        });

        return ResponseEntity.ok(all);
    }

    // ── GET SINGLE (unmasked for edit) ────────────────────────────────────────

    @GetMapping("/{key}")
    public ResponseEntity<ApiConfigEntity> getByKey(@PathVariable String key) {
        ApiConfigEntity entity = configService.getEntityByKey(key);
        // Return actual value for editing — admin only, secured by @PreAuthorize
        return ResponseEntity.ok(entity);
    }

    // ── UPDATE SINGLE ─────────────────────────────────────────────────────────

    @PutMapping("/{key}")
    public ResponseEntity<Map<String, Object>> updateByKey(
            @PathVariable String key,
            @RequestBody Map<String, String> body,
            Principal principal) {

        String value = body.get("value");
        if (value == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing 'value' in request body"));
        }

        String updatedBy = principal != null ? principal.getName() : "admin";
        ApiConfigEntity saved = configService.update(key, value, updatedBy);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "key",     saved.getConfigKey(),
                "updatedAt", saved.getUpdatedAt() != null ? saved.getUpdatedAt().toString() : ""
        ));
    }

    // ── BULK UPDATE ───────────────────────────────────────────────────────────

    /**
     * Bulk update endpoint — used by admin page "Save All" button.
     * Body: { "GOLD_API_KEY": "newkey123", "GOLD_CACHE_TTL_MINUTES": "20", ... }
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkUpdate(
            @RequestBody Map<String, String> updates,
            Principal principal) {

        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Empty update map"));
        }

        String updatedBy = principal != null ? principal.getName() : "admin";
        configService.updateBulk(updates, updatedBy);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "updatedCount", updates.size()
        ));
    }

    // ── CACHE EVICT ───────────────────────────────────────────────────────────

    /**
     * Force evict gold + silver price cache.
     * Next price fetch will hit the API fresh.
     */
    @PostMapping("/cache/evict")
    public ResponseEntity<Map<String, Object>> evictCache() {
        goldPriceService.evictCache();
        silverPriceService.evictCache();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gold and Silver price caches evicted. Next fetch will hit API live."
        ));
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    /**
     * Mask API key for display: show first 4 + last 4, rest as ****
     * e.g. "goldapixyz123456" → "gold********3456"
     */
    private String maskKey(String key) {
        if (key.length() <= 8) return "********";
        return key.substring(0, 4) +
                "*".repeat(Math.max(0, key.length() - 8)) +
                key.substring(key.length() - 4);
    }
}