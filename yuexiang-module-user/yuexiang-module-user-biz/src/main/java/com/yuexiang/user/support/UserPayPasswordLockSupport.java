package com.yuexiang.user.support;

import com.yuexiang.common.exception.ForbiddenException;
import com.yuexiang.user.constant.UserAccountConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserPayPasswordLockSupport {

    private final StringRedisTemplate redisTemplate;

    public void checkLock(Long userId) {
        String lockKey = buildLockKey(userId);
        String locked = redisTemplate.opsForValue().get(lockKey);
        if (locked == null) {
            return;
        }

        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        if (ttl == null || ttl < 0) {
            throw new ForbiddenException("支付密码已永久锁定，请通过验证码重置");
        }
        throw new ForbiddenException("支付密码已锁定，请" + ttl + "分钟后重试");
    }

    public void recordError(Long userId) {
        String errKey = buildErrorKey(userId);
        String lockKey = buildLockKey(userId);

        Long errCount = redisTemplate.opsForValue().increment(errKey);
        redisTemplate.expire(errKey, 24, TimeUnit.HOURS);
        if (errCount == null) {
            return;
        }

        if (errCount >= UserAccountConstants.MAX_ERR_L3) {
            redisTemplate.opsForValue().set(lockKey, "1");
            throw new ForbiddenException("支付密码已永久锁定，请通过验证码重置");
        }
        if (errCount >= UserAccountConstants.MAX_ERR_L2) {
            redisTemplate.opsForValue().set(lockKey, "1", UserAccountConstants.LOCK_MINUTES_L2, TimeUnit.MINUTES);
        } else if (errCount >= UserAccountConstants.MAX_ERR_L1) {
            redisTemplate.opsForValue().set(lockKey, "1", UserAccountConstants.LOCK_MINUTES_L1, TimeUnit.MINUTES);
        }
    }

    public int getRemainingAttempts(Long userId) {
        String val = redisTemplate.opsForValue().get(buildErrorKey(userId));
        int errCount = val != null ? Integer.parseInt(val) : 0;
        return Math.max(0, UserAccountConstants.MAX_ERR_L3 - errCount);
    }

    public void clear(Long userId) {
        redisTemplate.delete(buildErrorKey(userId));
        redisTemplate.delete(buildLockKey(userId));
    }

    private String buildErrorKey(Long userId) {
        return String.format(UserAccountConstants.PAY_PWD_ERR_KEY, userId);
    }

    private String buildLockKey(Long userId) {
        return String.format(UserAccountConstants.PAY_PWD_LOCK_KEY, userId);
    }
}
