package com.yuexiang.system.support;

import com.yuexiang.common.exception.AccountLockedException;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.DuplicateRequestException;
import com.yuexiang.system.constant.AuthRedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AuthRedisSupport {

    private final StringRedisTemplate redisTemplate;

    public void checkSmsRateLimit(String phone, String ip) {
        String minKey = String.format(AuthRedisConstants.SMS_RATE_MIN_KEY, phone);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(minKey))) {
            throw new DuplicateRequestException("发送过于频繁，请 60 秒后重试");
        }

        checkDailyLimit(
                String.format(AuthRedisConstants.SMS_RATE_IP_KEY, ip),
                AuthRedisConstants.SMS_IP_DAY_LIMIT,
                "IP 发送次数已达上限"
        );
        checkDailyLimit(
                String.format(AuthRedisConstants.SMS_RATE_DAY_KEY, phone),
                AuthRedisConstants.SMS_DAY_LIMIT,
                "今日发送次数已达上限"
        );

        redisTemplate.opsForValue().set(minKey, "1", 60, TimeUnit.SECONDS);
    }

    public void checkSmsVerifyAttempts(Long smsCodeId) {
        String errKey = String.format(AuthRedisConstants.SMS_VERIFY_ERR_KEY, smsCodeId);
        if (getRedisInt(errKey) >= AuthRedisConstants.SMS_VERIFY_MAX_ERR) {
            throw new BadRequestException("验证码尝试次数已达上限，请重新获取");
        }
    }

    public void recordSmsVerifyError(Long smsCodeId) {
        String errKey = String.format(AuthRedisConstants.SMS_VERIFY_ERR_KEY, smsCodeId);
        long errCount = incrementRedis(errKey, AuthRedisConstants.SMS_VERIFY_ERR_EXPIRE, TimeUnit.SECONDS);
        int remaining = AuthRedisConstants.SMS_VERIFY_MAX_ERR - (int) errCount;
        throw new BadRequestException("验证码错误，剩余 " + remaining + " 次尝试机会");
    }

    public void checkPasswordLock(String phone) {
        String lockKey = String.format(AuthRedisConstants.LOGIN_PWD_LOCK_KEY, phone);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            long remainSeconds = getExpireSeconds(lockKey);
            long remainMinutes = remainSeconds / 60 + 1;
            throw new AccountLockedException("账户已锁定，请 " + remainMinutes + " 分钟后重试");
        }
    }

    public void clearPasswordError(String phone) {
        redisTemplate.delete(String.format(AuthRedisConstants.LOGIN_PWD_ERR_KEY, phone));
    }

    public void handlePasswordError(String phone) {
        String errKey = String.format(AuthRedisConstants.LOGIN_PWD_ERR_KEY, phone);
        long errCount = incrementRedis(errKey, 24, TimeUnit.HOURS);

        if (errCount >= AuthRedisConstants.LOGIN_PWD_MAX_ERR) {
            String lockKey = String.format(AuthRedisConstants.LOGIN_PWD_LOCK_KEY, phone);
            redisTemplate.opsForValue().set(
                    lockKey,
                    "1",
                    AuthRedisConstants.LOGIN_PWD_LOCK_MINUTES,
                    TimeUnit.MINUTES
            );
            redisTemplate.delete(errKey);
            throw new AccountLockedException(
                    "密码错误次数过多，账户已锁定 " + AuthRedisConstants.LOGIN_PWD_LOCK_MINUTES + " 分钟"
            );
        }

        int remaining = AuthRedisConstants.LOGIN_PWD_MAX_ERR - (int) errCount;
        throw new BadRequestException("手机号或密码错误，剩余 " + remaining + " 次尝试机会");
    }

    private void checkDailyLimit(String key, int limit, String errorMsg) {
        int current = getRedisInt(key);
        if (current >= limit) {
            throw new DuplicateRequestException(errorMsg);
        }

        long newCount = incrementRedis(key, 1, TimeUnit.DAYS);
        if (newCount > limit) {
            throw new DuplicateRequestException(errorMsg);
        }
    }

    private long incrementRedis(String key, long timeout, TimeUnit unit) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 1L;
        }
        if (count == 1L) {
            redisTemplate.expire(key, timeout, unit);
        }
        return count;
    }

    private int getRedisInt(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    private long getExpireSeconds(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null ? expire : 0;
    }
}
