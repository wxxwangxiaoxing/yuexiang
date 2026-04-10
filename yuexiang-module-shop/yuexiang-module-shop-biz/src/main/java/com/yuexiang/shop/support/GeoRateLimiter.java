package com.yuexiang.shop.support;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class GeoRateLimiter {

    private static final DateTimeFormatter MINUTE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter SECOND_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final StringRedisTemplate stringRedisTemplate;

    public void checkReadLimit(Long userId, String clientIp) {
        if (userId != null) {
            check(
                    "user",
                    String.valueOf(userId),
                    20,
                    Duration.ofMinutes(1),
                    MINUTE_FORMAT,
                    "请求过于频繁，请稍后重试");
        }
        check(
                "ip",
                normalizeSubject(clientIp),
                60,
                Duration.ofMinutes(1),
                MINUTE_FORMAT,
                "请求过于频繁，请稍后重试");
        check(
                "global",
                "all",
                2000,
                Duration.ofSeconds(1),
                SECOND_FORMAT,
                "请求过于频繁，请稍后重试");
    }

    private void check(
            String dimension,
            String subject,
            int limit,
            Duration ttl,
            DateTimeFormatter formatter,
            String message) {
        String bucket = LocalDateTime.now().format(formatter);
        String key = "rate:geo:nearby:" + dimension + ":" + subject + ":" + bucket;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, ttl);
        }
        if (count != null && count > limit) {
            throw new RuntimeException(message);
        }
    }

    private String normalizeSubject(String subject) {
        return StringUtils.hasText(subject) ? subject.trim() : "unknown";
    }
}
