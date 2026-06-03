package com.dducwsjvbe.backendjava.service;

import com.dducwsjvbe.backendjava.dto.Dto.ProductDto;
import com.dducwsjvbe.backendjava.dto.response.PageResponse;
import com.dducwsjvbe.backendjava.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j(topic = "Product-Search-Cache-Service")
@Service
@RequiredArgsConstructor
public class ProductSearchCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "search_product";
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Pattern PATTERN =
            Pattern.compile("(\\w+?)([:<>~!])(\\p{Punct}?)(.*?)(\\p{Punct}?)$");


    // =========================================
    // GET
    // =========================================
    public PageResponse<List<ProductDto>> get(Pageable pageable, String[] product,
                               String[] user, UserStatus status) {
        String key = buildKey(pageable, product, user, status);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("[CACHE HIT] key={}", key);
                return (PageResponse<List<ProductDto>>) cached;
            }
            log.info("[CACHE MISS] key={}", key);
        } catch (Exception e) {
            log.warn("[CACHE ERROR] get failed key={} error={}", key, e);
        }
        return null;
    }

    // =========================================
    // SET
    // =========================================
    public void set(Pageable pageable, String[] product,
                    String[] user, UserStatus status, PageResponse<List<ProductDto>> data) {
        String key = buildKey(pageable, product, user, status);
        try {
            redisTemplate.opsForValue().set(key, data, TTL);
            log.info("[CACHE SET] key={} ttl={}m", key, TTL.toMinutes());
        } catch (Exception e) {
            log.warn("[CACHE ERROR] set failed key={} error={}", key, e);
        }
    }
    // =========================================
    // BUILD KEY
    // =========================================
    private String buildKey(Pageable pageable, String[] product,
                            String[] user, UserStatus status) {
        StringBuilder key = new StringBuilder(CACHE_PREFIX);

        key.append(":page=").append(pageable.getPageNumber());
        key.append(":size=").append(pageable.getPageSize());

        if (status != null) {
            key.append(":status=").append(status.name());
        }

        if (product != null && product.length > 0) {
            List<String> productParts = new ArrayList<>();
            for (String p : product) {
                Matcher matcher = PATTERN.matcher(p);
                if (matcher.find()) {
                    productParts.add(matcher.group(1) + matcher.group(2) + matcher.group(4));
                }
            }
            Collections.sort(productParts);
            key.append(":product=[")
                    .append(String.join(",", productParts))
                    .append("]");
        }

        if (user != null && user.length > 0) {
            List<String> userParts = new ArrayList<>();
            for (String u : user) {
                Matcher matcher = PATTERN.matcher(u);
                if (matcher.find()) {
                    userParts.add(matcher.group(1) + matcher.group(2) + matcher.group(4));
                }
            }
            Collections.sort(userParts);
            key.append(":user=[")
                    .append(String.join(",", userParts))
                    .append("]");
        }

        return key.toString();
    }

    // =========================================
    // EVICT:xóa khi post,up,del
    // =========================================
    public void evictByStatus(UserStatus status) {
        evictByPattern(CACHE_PREFIX + ":*:status=" + status.name() + "*");
    }

    public void evictAll() {
        evictByPattern(CACHE_PREFIX + ":*");
    }

    private void evictByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("[CACHE EVICT] pattern={} count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.warn("[CACHE ERROR] evict failed pattern={} error={}", pattern, e.getMessage());
        }
    }
}
