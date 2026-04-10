package com.yuexiang.voucher.service.impl;

import com.yuexiang.voucher.constants.SeckillRedisConstants;
import com.yuexiang.voucher.service.SeckillCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeckillCacheServiceImpl implements SeckillCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void evictListCache() {
        Set<String> keys = stringRedisTemplate.keys(SeckillRedisConstants.SECKILL_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Override
    public void evictListCache(Integer pageSize) {
        String cacheKey = SeckillRedisConstants.SECKILL_LIST_KEY + pageSize;
        stringRedisTemplate.delete(cacheKey);
    }

    @Override
    public void evictStockCache(Long voucherId) {
        String stockKey = SeckillRedisConstants.SECKILL_STOCK_KEY + voucherId;
        stringRedisTemplate.delete(stockKey);
    }
}
