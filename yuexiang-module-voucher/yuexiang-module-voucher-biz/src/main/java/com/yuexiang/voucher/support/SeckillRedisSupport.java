package com.yuexiang.voucher.support;

import com.yuexiang.voucher.constants.SeckillRedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SeckillRedisSupport {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryAcquireButtonLock(Long userId, Long voucherId) {
        String buttonKey = SeckillRedisConstants.SECKILL_BUTTON_KEY + userId + ":" + voucherId;
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(buttonKey, "1", SeckillRedisConstants.BUTTON_TTL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public Integer getStockFromRedis(Long voucherId, Integer dbStock) {
        String stockStr = stringRedisTemplate.opsForValue().get(SeckillRedisConstants.SECKILL_STOCK_KEY + voucherId);
        return stockStr != null ? Integer.parseInt(stockStr) : dbStock;
    }

    public boolean hasBought(Long voucherId, Long userId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(SeckillRedisConstants.SECKILL_ORDER_KEY + voucherId, userId.toString())
        );
    }

    public void markBought(Long voucherId, Long userId) {
        stringRedisTemplate.opsForSet().add(SeckillRedisConstants.SECKILL_ORDER_KEY + voucherId, userId.toString());
    }

    public void cacheOrderResult(Long userId, Long voucherId, Long orderId) {
        String orderResultKey = SeckillRedisConstants.SECKILL_ORDER_RESULT_KEY + userId + ":" + voucherId;
        stringRedisTemplate.opsForValue().set(orderResultKey, orderId.toString(), 30, TimeUnit.MINUTES);
    }

    public void rollbackReservation(Long voucherId, Long userId) {
        stringRedisTemplate.opsForValue().increment(SeckillRedisConstants.SECKILL_STOCK_KEY + voucherId);
        stringRedisTemplate.opsForSet().remove(SeckillRedisConstants.SECKILL_ORDER_KEY + voucherId, userId.toString());
        stringRedisTemplate.delete(SeckillRedisConstants.SECKILL_ORDER_RESULT_KEY + userId + ":" + voucherId);
    }
}
