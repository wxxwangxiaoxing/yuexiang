package com.yuexiang.voucher.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.voucher.constants.SeckillRedisConstants;
import com.yuexiang.voucher.mapper.SeckillVoucherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillCompensationSupport {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final SeckillVoucherMapper seckillVoucherMapper;
    private final SeckillRedisSupport seckillRedisSupport;

    public void enqueueRetry(Long voucherId, Long userId, boolean dbStockDeducted) {
        try {
            Map<String, Object> retryData = new HashMap<>();
            retryData.put("voucherId", voucherId);
            retryData.put("userId", userId);
            retryData.put("dbStockDeducted", dbStockDeducted);
            retryData.put("timestamp", System.currentTimeMillis());
            stringRedisTemplate.opsForList().rightPush(
                    SeckillRedisConstants.SECKILL_RETRY_QUEUE_KEY,
                    objectMapper.writeValueAsString(retryData)
            );
            log.info("已加入补偿队列，voucherId={}, userId={}, dbStockDeducted={}", voucherId, userId, dbStockDeducted);
        } catch (JsonProcessingException e) {
            log.error("序列化补偿数据失败", e);
        }
    }

    public void rollbackReservation(Long voucherId, Long userId, boolean dbStockDeducted) {
        if (dbStockDeducted) {
            seckillVoucherMapper.incrStock(voucherId);
        }
        seckillRedisSupport.rollbackReservation(voucherId, userId);
        log.warn("已回滚秒杀预占状态，voucherId={}, userId={}, dbStockDeducted={}", voucherId, userId, dbStockDeducted);
    }
}
