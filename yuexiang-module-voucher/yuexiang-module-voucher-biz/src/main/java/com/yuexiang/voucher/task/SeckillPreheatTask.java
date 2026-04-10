package com.yuexiang.voucher.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.voucher.constants.SeckillRedisConstants;
import com.yuexiang.voucher.domain.entity.SeckillSession;
import com.yuexiang.voucher.domain.entity.SeckillVoucher;
import com.yuexiang.voucher.mapper.SeckillSessionMapper;
import com.yuexiang.voucher.mapper.SeckillVoucherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀数据预热任务
 * <p>
 * 主要职责：
 * 1. 每天凌晨预热当天所有有效秒杀场次
 * 2. 定时预热未来两小时内即将开始的秒杀场次
 * 3. 将秒杀券库存提前加载到 Redis，提高秒杀性能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillPreheatTask {

    /**
     * 场次状态：启用
     */
    private static final Integer SESSION_STATUS_ENABLED = 1;

    /**
     * 逻辑删除标记：未删除
     */
    private static final Integer NOT_DELETED = 0;

    /**
     * Redis 预热缓存过期时间，单位：小时
     */
    private static final long PREHEAT_EXPIRE_HOURS = 24L;

    private final SeckillSessionMapper sessionMapper;
    private final SeckillVoucherMapper seckillVoucherMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 每天凌晨执行，预热当天全部秒杀场次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void preheatDaily() {
        log.info("开始预热当天秒杀数据，date={}", LocalDate.now());
        preheatSessions(LocalDate.now());
        log.info("当天秒杀数据预热完成，date={}", LocalDate.now());
    }

    /**
     * 每5分钟执行一次，预热未来2小时内即将开始的秒杀场次
     */
    @Scheduled(fixedRate = 300000)
    public void preheatUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(2);

        List<SeckillSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<SeckillSession>()
                        .eq(SeckillSession::getStatus, SESSION_STATUS_ENABLED)
                        .eq(SeckillSession::getDeleted, NOT_DELETED)
                        .ge(SeckillSession::getBeginTime, now)
                        .le(SeckillSession::getBeginTime, endTime)
        );

        if (CollectionUtils.isEmpty(sessions)) {
            log.debug("未来2小时内无需要预热的秒杀场次，start={}, end={}", now, endTime);
            return;
        }

        log.info("开始预热未来2小时内秒杀场次，count={}, start={}, end={}", sessions.size(), now, endTime);
        preheatSessionList(sessions);
        log.info("未来2小时内秒杀场次预热完成，count={}", sessions.size());
    }

    /**
     * 按日期预热秒杀场次
     *
     * @param date 业务日期
     */
    public void preheatSessions(LocalDate date) {
        List<SeckillSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<SeckillSession>()
                        .eq(SeckillSession::getDate, date)
                        .eq(SeckillSession::getStatus, SESSION_STATUS_ENABLED)
                        .eq(SeckillSession::getDeleted, NOT_DELETED)
        );

        if (CollectionUtils.isEmpty(sessions)) {
            log.info("当前日期无可预热秒杀场次，date={}", date);
            return;
        }

        log.info("开始预热指定日期秒杀场次，date={}, count={}", date, sessions.size());
        preheatSessionList(sessions);
        log.info("指定日期秒杀场次预热完成，date={}, count={}", date, sessions.size());
    }

    /**
     * 批量预热场次
     *
     * @param sessions 秒杀场次列表
     */
    private void preheatSessionList(List<SeckillSession> sessions) {
        for (SeckillSession session : sessions) {
            preheatSessionVouchers(session);
        }
    }

    /**
     * 预热单个场次下的所有秒杀券
     * <p>
     * 预热内容：
     * 1. 将秒杀券库存写入 Redis
     * 2. 根据需要初始化或清理与该秒杀券相关的辅助 key
     *
     * @param session 秒杀场次
     */
    private void preheatSessionVouchers(SeckillSession session) {
        List<SeckillVoucher> vouchers = seckillVoucherMapper.selectList(
                new LambdaQueryWrapper<SeckillVoucher>()
                        .eq(SeckillVoucher::getSessionId, session.getId())
                        .eq(SeckillVoucher::getDeleted, NOT_DELETED)
        );

        if (CollectionUtils.isEmpty(vouchers)) {
            log.info("场次下无秒杀券，无需预热，sessionId={}", session.getId());
            return;
        }

        for (SeckillVoucher seckillVoucher : vouchers) {
            preheatSingleVoucher(seckillVoucher);
        }

        log.info("预热场次成功，sessionId={}, voucherCount={}", session.getId(), vouchers.size());
    }

    /**
     * 预热单个秒杀券
     *
     * @param seckillVoucher 秒杀券信息
     */
    private void preheatSingleVoucher(SeckillVoucher seckillVoucher) {
        Long voucherId = seckillVoucher.getVoucherId();
        String stockKey = SeckillRedisConstants.SECKILL_STOCK_KEY + voucherId;
        String orderKey = SeckillRedisConstants.SECKILL_ORDER_KEY + voucherId;

        // 库存预热：如果 Redis 中不存在，则写入库存
        Boolean stockInit = stringRedisTemplate.opsForValue().setIfAbsent(
                stockKey,
                String.valueOf(seckillVoucher.getStock()),
                PREHEAT_EXPIRE_HOURS,
                TimeUnit.HOURS
        );

        // 这里的 orderKey 是否需要删除，要视业务而定：
        // 如果它是“秒杀用户下单记录”或“防重复下单标记”，通常只在新场次初始化时清理。
        // 当前保留原逻辑意图，但修正判断条件为：存在才删除。
        if (stringRedisTemplate.hasKey(orderKey)) {
            stringRedisTemplate.delete(orderKey);
            log.debug("清理秒杀下单标记成功，voucherId={}, orderKey={}", voucherId, orderKey);
        }

        log.debug("秒杀券预热完成，voucherId={}, stock={}, stockInit={}",
                voucherId, seckillVoucher.getStock(), stockInit);
    }
}