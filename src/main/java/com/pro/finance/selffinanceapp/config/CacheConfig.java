package com.pro.finance.selffinanceapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("goldPrice", "silverPrice");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        // ✅ Reduced from 5 mins to 1 min — gold prices move fast
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .maximumSize(10)
        );
        return manager;
    }
}