package com.huiminpay.transaction.config;

import com.huiminpay.common.cache.Cache;
import com.huiminpay.transaction.common.util.RedisCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
    @Bean
    public Cache cache(StringRedisTemplate redisTemplate){
        return new RedisCache(redisTemplate);
    }
}