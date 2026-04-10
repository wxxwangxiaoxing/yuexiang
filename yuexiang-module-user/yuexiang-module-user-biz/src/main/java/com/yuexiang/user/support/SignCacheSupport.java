package com.yuexiang.user.support;

import com.yuexiang.user.constant.SignConstants;
import com.yuexiang.user.domain.entity.Sign;
import com.yuexiang.user.mapper.SignMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SignCacheSupport {

    private final StringRedisTemplate redisTemplate;
    private final SignMapper signMapper;

    public Integer getSignBitmap(Long userId, int year, int month) {
        String cacheKey = buildBitmapKey(userId, year, month);
        String value = redisTemplate.opsForValue().get(cacheKey);
        if (value != null) {
            return Integer.parseInt(value);
        }

        Sign sign = signMapper.selectByUserMonth(userId, year, month);
        if (sign == null) {
            return null;
        }

        redisTemplate.opsForValue().set(
                cacheKey,
                String.valueOf(sign.getSignBitmap()),
                calcBitmapCacheTtlSeconds(year, month),
                TimeUnit.SECONDS
        );
        return sign.getSignBitmap();
    }

    public void updateSignCache(Long userId, int year, int month, int newBitmap, int continuousDays) {
        redisTemplate.opsForValue().set(
                buildBitmapKey(userId, year, month),
                String.valueOf(newBitmap),
                calcBitmapCacheTtlSeconds(year, month),
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                buildContinuousKey(userId),
                String.valueOf(continuousDays),
                SignConstants.CONTINUOUS_CACHE_HOURS,
                TimeUnit.HOURS
        );
    }

    public void evictSignCache(Long userId, int year, int month) {
        redisTemplate.delete(buildBitmapKey(userId, year, month));
    }

    public void updateContinuousCache(Long userId, int continuousDays) {
        redisTemplate.opsForValue().set(
                buildContinuousKey(userId),
                String.valueOf(continuousDays),
                SignConstants.CONTINUOUS_CACHE_HOURS,
                TimeUnit.HOURS
        );
    }

    public String tryLock(String key) {
        String value = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key,
                value,
                SignConstants.LOCK_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );
        return Boolean.TRUE.equals(success) ? value : null;
    }

    public void unlock(String key, String lockValue) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (Objects.equals(currentValue, lockValue)) {
                redisTemplate.delete(key);
            }
        } catch (Exception ignored) {
        }
    }

    public String buildSignLockKey(Long userId) {
        return "lock:sign:" + userId;
    }

    public String buildRepairLockKey(Long userId) {
        return "lock:sign:repair:" + userId;
    }

    public String buildClaimLockKey(Long userId, Long ruleId) {
        return "lock:sign:claim:" + userId + ":" + ruleId;
    }

    private String buildBitmapKey(Long userId, int year, int month) {
        return String.format("sign:bitmap:%d:%d:%d", userId, year, month);
    }

    private String buildContinuousKey(Long userId) {
        return String.format("sign:continuous:%d", userId);
    }

    private long calcBitmapCacheTtlSeconds(int year, int month) {
        LocalDate expireDate = LocalDate.of(year, month, 1).plusMonths(1).plusDays(6);
        long ttl = Duration.between(LocalDateTime.now(SignConstants.ZONE), expireDate.atStartOfDay()).getSeconds();
        return Math.max(ttl, SignConstants.MIN_BITMAP_CACHE_SECONDS);
    }
}
