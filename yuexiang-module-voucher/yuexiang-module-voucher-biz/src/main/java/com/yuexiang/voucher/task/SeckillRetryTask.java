package com.yuexiang.voucher.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.voucher.constants.SeckillRedisConstants;
import com.yuexiang.voucher.domain.entity.Voucher;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import com.yuexiang.voucher.mapper.SeckillVoucherMapper;
import com.yuexiang.voucher.mapper.VoucherMapper;
import com.yuexiang.voucher.mapper.VoucherOrderMapper;
import com.yuexiang.voucher.support.SeckillCompensationSupport;
import com.yuexiang.voucher.support.SeckillRedisSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillRetryTask {

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY = 3;

    /**
     * 订单状态：待支付
     */
    private static final int ORDER_STATUS_PENDING = 0;

    /**
     * 支付方式：默认
     */
    private static final int PAY_TYPE_DEFAULT = 0;

    /**
     * 逻辑删除标记：未删除
     */
    private static final int NOT_DELETED = 0;

    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherOrderMapper voucherOrderMapper;
    private final VoucherMapper voucherMapper;
    private final SeckillVoucherMapper seckillVoucherMapper;
    private final ObjectMapper objectMapper;
    private final SeckillCompensationSupport seckillCompensationSupport;
    private final SeckillRedisSupport seckillRedisSupport;

    /**
     * 定时处理秒杀补偿重试队列
     * <p>
     * fixedDelay = 5000 表示：
     * 上一次任务执行完成后，延迟 5 秒再次执行
     */
    @Scheduled(fixedDelay = 5000)
    public void processRetryQueue() {
        try {
            String json = popRetryMessage();
            if (json == null) {
                return;
            }

            Map<String, Object> retryData = parseRetryData(json);
            if (retryData == null) {
                return;
            }

            handleRetryData(retryData);
        } catch (Exception e) {
            log.error("处理秒杀补偿队列异常", e);
        }
    }

    /**
     * 从 Redis 重试队列中弹出一条消息
     *
     * @return 队列消息 JSON，若为空则返回 null
     */
    private String popRetryMessage() {
        return stringRedisTemplate.opsForList()
                .leftPop(SeckillRedisConstants.SECKILL_RETRY_QUEUE_KEY);
    }

    /**
     * 解析重试消息
     *
     * @param json Redis 中存储的 JSON 字符串
     * @return 解析后的 Map，解析失败返回 null
     */
    private Map<String, Object> parseRetryData(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("解析秒杀补偿消息失败，json={}", json, e);
            return null;
        }
    }

    /**
     * 处理重试消息
     *
     * @param retryData 重试数据
     */
    private void handleRetryData(Map<String, Object> retryData) {
        Long voucherId = getLongValue(retryData, "voucherId");
        Long userId = getLongValue(retryData, "userId");
        int retryCount = getIntValue(retryData);
        boolean dbStockDeducted = isDbStockDeducted(retryData);

        // 基础参数校验
        if (voucherId == null || userId == null) {
            log.error("秒杀补偿消息缺少必要参数，retryData={}", retryData);
            return;
        }

        // 超过最大重试次数，直接丢弃
        if (retryCount >= MAX_RETRY) {
            log.error("秒杀订单重试次数已达上限，voucherId={}, userId={}", voucherId, userId);
            seckillCompensationSupport.rollbackReservation(voucherId, userId, dbStockDeducted);
            return;
        }

        try {
            doCreateOrder(voucherId, userId, retryData, retryCount, dbStockDeducted);
        } catch (Exception e) {
            requeue(retryData, retryCount + 1, dbStockDeducted);
            log.error("补偿订单创建异常，voucherId={}, userId={}, retryCount={}", voucherId, userId, retryCount + 1, e);
        }
    }

    /**
     * 执行补偿创建订单逻辑
     * <p>
     * 流程：
     * 1. 扣减数据库库存
     * 2. 校验优惠券是否存在
     * 3. 创建订单
     *
     * @param voucherId 优惠券ID
     * @param userId 用户ID
     * @param retryData 原始重试数据
     * @param retryCount 当前重试次数
     */
    private void doCreateOrder(
            Long voucherId,
            Long userId,
            Map<String, Object> retryData,
            Integer retryCount,
            boolean dbStockDeducted
    ) {
        Long existingOrderId = voucherOrderMapper.selectOrderIdByVoucherAndUser(voucherId, userId);
        if (existingOrderId != null) {
            seckillRedisSupport.cacheOrderResult(userId, voucherId, existingOrderId);
            log.info("补偿发现订单已存在，直接回写结果，orderId={}, voucherId={}, userId={}", existingOrderId, voucherId, userId);
            return;
        }

        boolean currentDbStockDeducted = dbStockDeducted;
        if (!currentDbStockDeducted) {
            int updated = seckillVoucherMapper.decrStock(voucherId);
            if (updated <= 0) {
                requeue(retryData, retryCount + 1, false);
                log.info("DB库存扣减失败，重新入队重试，voucherId={}, retryCount={}", voucherId, retryCount + 1);
                return;
            }
            currentDbStockDeducted = true;
        }

        Voucher voucher = voucherMapper.selectById(voucherId);
        if (Objects.isNull(voucher)) {
            log.error("补偿订单创建失败，优惠券不存在，voucherId={}, userId={}", voucherId, userId);
            seckillCompensationSupport.rollbackReservation(voucherId, userId, currentDbStockDeducted);
            return;
        }

        VoucherOrder order = buildVoucherOrder(voucherId, userId, retryCount);
        voucherOrderMapper.insert(order);
        seckillRedisSupport.cacheOrderResult(userId, voucherId, order.getId());

        log.info("补偿订单创建成功，orderId={}, voucherId={}, userId={}", order.getId(), voucherId, userId);
    }

    /**
     * 构建优惠券订单对象
     *
     * @param voucherId 优惠券ID
     * @param userId 用户ID
     * @param retryCount 当前重试次数
     * @return VoucherOrder
     */
    private VoucherOrder buildVoucherOrder(Long voucherId, Long userId, Integer retryCount) {
        VoucherOrder order = new VoucherOrder();
        order.setVoucherId(voucherId);
        order.setUserId(userId);
        order.setOrderNo(generateOrderNo(retryCount));
        order.setStatus(ORDER_STATUS_PENDING);
        order.setPayType(PAY_TYPE_DEFAULT);
        order.setCreateTime(LocalDateTime.now());
        order.setDeleted(NOT_DELETED);
        return order;
    }

    /**
     * 重新入队
     *
     * @param retryData 重试数据
     * @param newRetryCount 新的重试次数
     */
    private void requeue(Map<String, Object> retryData, int newRetryCount, boolean dbStockDeducted) {
        try {
            retryData.put("retryCount", newRetryCount);
            retryData.put("dbStockDeducted", dbStockDeducted);
            stringRedisTemplate.opsForList().rightPush(
                    SeckillRedisConstants.SECKILL_RETRY_QUEUE_KEY,
                    objectMapper.writeValueAsString(retryData)
            );
        } catch (Exception e) {
            log.error("重试消息重新入队失败，retryData={}", retryData, e);
        }
    }

    /**
     * 生成订单号
     * <p>
     * 这里仍保留你原有规则：
     * SK + 当前时间戳 + 4位重试次数
     *
     * @param retryCount 重试次数
     * @return 订单号
     */
    private String generateOrderNo(Integer retryCount) {
        return "SK" + System.currentTimeMillis() + String.format("%04d", retryCount);
    }

    /**
     * 从 Map 中读取 Long 类型字段
     *
     * @param data 数据源
     * @param key 字段名
     * @return Long 值，不存在则返回 null
     */
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    /**
     * 从 Map 中读取 Integer 类型字段
     *
     * @param data 数据源
     * @return Integer 值
     */
    private Integer getIntValue(Map<String, Object> data) {
        Object value = data.get("retryCount");
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private boolean isDbStockDeducted(Map<String, Object> data) {
        Object value = data.get("dbStockDeducted");
        return value instanceof Boolean && (Boolean) value;
    }
}
