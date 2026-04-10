package com.yuexiang.voucher.support;

import com.yuexiang.voucher.constants.SeckillRedisConstants;
import com.yuexiang.voucher.service.SeckillCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoucherOrderStockSupport {

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillCacheService seckillCacheService;

    public void restoreStock(Long voucherId, Long userId) {
        stringRedisTemplate.opsForValue().increment(SeckillRedisConstants.SECKILL_STOCK_KEY + voucherId);
        stringRedisTemplate.opsForSet().remove(SeckillRedisConstants.SECKILL_ORDER_KEY + voucherId, String.valueOf(userId));
        seckillCacheService.evictListCache();
    }
}
