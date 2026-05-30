package com.springboot.employeeperformance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;


@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
    /*@Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default fallback TTL
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())); // Enforces JSON text storage

        Map<String, RedisCacheConfiguration> customConfigs = new HashMap<>();

        customConfigs.put("employee-reviews", defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        customConfigs.put("cycle-summaries", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));
        customConfigs.put("employee-ratings", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(customConfigs)
                .build();*/
}