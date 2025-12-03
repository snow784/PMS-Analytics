package com.pms.analytics.service.currentPrice;

import java.math.BigDecimal;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisPriceCache {

    private final RedisTemplate<String, Object> redis;
    private static final String PRICE_KEY = "prices";

    public void updatePrice(String symbol, BigDecimal price) {
        redis.opsForHash().put(PRICE_KEY, symbol, price);
        System.out.println(PRICE_KEY +" "+ symbol +" "+ price);
    }

    public BigDecimal getPrice(String symbol) {
        Object value = redis.opsForHash().get(PRICE_KEY, symbol);
        return value != null ? new BigDecimal(value.toString()) : null;
    }
}

