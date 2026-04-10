package com.yuexiang.voucher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.voucher.constants.SeckillRedisConstants;
import com.yuexiang.voucher.domain.dto.SeckillOrderDTO;
import com.yuexiang.voucher.domain.dto.SeckillVoucherWithInfo;
import com.yuexiang.voucher.domain.entity.SeckillSession;
import com.yuexiang.voucher.domain.entity.SeckillVoucher;
import com.yuexiang.voucher.domain.entity.Voucher;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import com.yuexiang.voucher.domain.vo.*;
import com.yuexiang.voucher.mapper.SeckillSessionMapper;
import com.yuexiang.voucher.mapper.SeckillVoucherMapper;
import com.yuexiang.voucher.mapper.VoucherMapper;
import com.yuexiang.voucher.mapper.VoucherOrderMapper;
import com.yuexiang.voucher.service.SeckillService;
import com.yuexiang.voucher.support.SeckillCompensationSupport;
import com.yuexiang.voucher.support.SeckillRedisSupport;
import com.yuexiang.voucher.support.SeckillLuaScripts;
import com.yuexiang.voucher.support.SeckillShopSupport;
import com.yuexiang.voucher.support.SeckillStatusSupport;
import com.yuexiang.voucher.support.SnowflakeIdGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现类
 * <p>
 * 提供秒杀相关的业务功能，包括：
 * - 获取服务器时间
 * - 获取秒杀场次列表
 * - 获取秒杀券列表
 * - 获取秒杀券详情
 * - 执行秒杀下单（使用 Lua 脚本保证原子性）
 * - 获取订单结果
 * </p>
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    // ---------- Mapper 依赖 ----------
    /** 秒杀场次 Mapper */
    private final SeckillSessionMapper sessionMapper;
    /** 秒杀券 Mapper */
    private final SeckillVoucherMapper seckillVoucherMapper;
    /** 优惠券 Mapper */
    private final VoucherMapper voucherMapper;
    /** 订单 Mapper */
    private final VoucherOrderMapper voucherOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final SeckillStatusSupport seckillStatusSupport;
    private final SeckillRedisSupport seckillRedisSupport;
    private final SeckillShopSupport seckillShopSupport;
    private final SeckillCompensationSupport seckillCompensationSupport;

    /** Lua 脚本内容（从常量类获取） */
    private static final String SECKILL_SCRIPT = SeckillLuaScripts.SECKILL_SCRIPT;

    /** Lua 脚本对象，用于执行原子秒杀操作 */
    private DefaultRedisScript<Long> seckillRedisScript;

    /**
     * 初始化 Redis Lua 脚本
     * 在 Bean 构造完成后执行
     */
    @PostConstruct
    public void init() {
        seckillRedisScript = new DefaultRedisScript<>(SECKILL_SCRIPT, Long.class);
    }

    // ================== 接口实现方法 ==================

    /**
     * 获取服务器当前时间（毫秒级时间戳）
     *
     * @return 包含服务器时间的 VO 对象
     */
    @Override
    public ServerTimeVO getServerTime() {
        ServerTimeVO vo = new ServerTimeVO();
        vo.setServerTime(System.currentTimeMillis());
        return vo;
    }

    /**
     * 获取指定日期的秒杀场次列表
     *
     * @param date 日期（可为空，默认当天）
     * @return 场次列表 VO，包含服务器时间及场次详情
     */
    @Override
    public SessionListVO getSessions(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        // 查询有效场次
        LambdaQueryWrapper<SeckillSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillSession::getDate, date)
                .eq(SeckillSession::getStatus, 1)
                .eq(SeckillSession::getDeleted, 0)
                .orderByAsc(SeckillSession::getBeginTime);

        List<SeckillSession> sessions = sessionMapper.selectList(wrapper);

        if (sessions.isEmpty()) {
            SessionListVO result = new SessionListVO();
            result.setServerTime(System.currentTimeMillis());
            result.setSessions(Collections.emptyList());
            return result;
        }

        List<Long> sessionIds = sessions.stream()
                .map(SeckillSession::getId)
                .toList();

        // 批量统计每个场次下的秒杀券数量
        List<Map<String, Object>> counts = seckillVoucherMapper.countVouchersBySessionIds(sessionIds);
        Map<Long, Integer> countMap = new HashMap<>();
        for (Map<String, Object> map : counts) {
            Object sessionId = map.get("session_id");
            Object count = map.get("count");
            if (sessionId != null && count != null) {
                countMap.put(((Number) sessionId).longValue(), ((Number) count).intValue());
            }
        }

        long now = System.currentTimeMillis();
        List<SessionListVO.SessionVO> sessionVOs = new ArrayList<>();

        // 组装每个场次的数据
        for (SeckillSession session : sessions) {
            SessionListVO.SessionVO vo = new SessionListVO.SessionVO();
            vo.setSessionId(session.getId());
            vo.setTitle(session.getTitle());
            vo.setBeginTime(seckillStatusSupport.toTimestamp(session.getBeginTime()));
            vo.setEndTime(seckillStatusSupport.toTimestamp(session.getEndTime()));

            int status = seckillStatusSupport.getSessionStatus(session, now);
            vo.setStatus(status);
            vo.setStatusDesc(seckillStatusSupport.getSessionStatusDesc(status));

            vo.setVoucherCount(countMap.getOrDefault(session.getId(), 0));

            sessionVOs.add(vo);
        }

        SessionListVO result = new SessionListVO();
        result.setServerTime(now);
        result.setSessions(sessionVOs);
        return result;
    }

    /**
     * 分页获取指定秒杀场次下的秒杀券列表
     *
     * @param sessionId 场次 ID
     * @param page      页码（从1开始）
     * @param pageSize  每页条数
     * @return 秒杀券列表 VO，包含总数、分页信息及券详情
     */
    @Override
    public SeckillVoucherListVO getVouchers(Long sessionId, Integer page, Integer pageSize) {
        Long total = seckillVoucherMapper.countBySessionId(sessionId);

        if (total == null || total == 0) {
            SeckillVoucherListVO result = new SeckillVoucherListVO();
            result.setTotal(0L);
            result.setPage(page);
            result.setPageSize(pageSize);
            result.setHasMore(false);
            result.setList(Collections.emptyList());
            return result;
        }

        int offset = (page - 1) * pageSize;
        List<SeckillVoucherWithInfo> voucherInfos = seckillVoucherMapper.selectVouchersBySessionWithPage(sessionId, offset, pageSize);

        if (voucherInfos.isEmpty()) {
            SeckillVoucherListVO result = new SeckillVoucherListVO();
            result.setTotal(total);
            result.setPage(page);
            result.setPageSize(pageSize);
            result.setHasMore(false);
            result.setList(Collections.emptyList());
            return result;
        }

        Map<Long, Shop> shopMap = seckillShopSupport.loadShopMap(voucherInfos);

        long now = System.currentTimeMillis();
        List<SeckillVoucherListVO.VoucherVO> voucherVOs = new ArrayList<>();

        // 组装每个秒杀券的数据
        for (SeckillVoucherWithInfo info : voucherInfos) {
            SeckillVoucherListVO.VoucherVO vo = new SeckillVoucherListVO.VoucherVO();
            vo.setVoucherId(info.getVoucherId());
            vo.setShopId(info.getShopId());
            vo.setTitle(info.getTitle());
            vo.setSubTitle(info.getSubTitle());
            vo.setPayValue(info.getPayValue());
            vo.setActualValue(info.getActualValue());
            vo.setOriginalPrice(info.getActualValue());
            vo.setSeckillPrice(info.getPayValue());
            vo.setTotalStock(info.getTotalStock());

            // 从 Redis 获取实时库存
            Integer remainStock = seckillRedisSupport.getStockFromRedis(info.getVoucherId(), info.getStock());
            vo.setRemainStock(remainStock);
            vo.setStockPercent(seckillStatusSupport.calculateStockPercent(remainStock, info.getTotalStock()));
            vo.setBeginTime(seckillStatusSupport.toTimestamp(info.getBeginTime()));
            vo.setEndTime(seckillStatusSupport.toTimestamp(info.getEndTime()));

            int status = seckillStatusSupport.getSeckillStatus(info.getBeginTime(), info.getEndTime(), now, remainStock);
            vo.setSeckillStatus(status);
            vo.setSeckillStatusDesc(seckillStatusSupport.getSeckillStatusDesc(status));

            seckillShopSupport.fillShopInfo(vo, shopMap.get(info.getShopId()));

            voucherVOs.add(vo);
        }

        SeckillVoucherListVO result = new SeckillVoucherListVO();
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setHasMore((long) page * pageSize < total);
        result.setList(voucherVOs);
        return result;
    }

    /**
     * 获取秒杀券详情
     *
     * @param voucherId 秒杀券 ID
     * @param userId    用户 ID（可选，用于判断是否已购买）
     * @return 秒杀券详情 VO
     * @throws NotFoundException 优惠券或秒杀券不存在时抛出
     */
    @Override
    public SeckillVoucherDetailVO getVoucherDetail(Long voucherId, Long userId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || voucher.getDeleted() == 1) {
            throw new NotFoundException("优惠券不存在");
        }

        SeckillVoucher sv = seckillVoucherMapper.selectById(voucherId);
        if (sv == null || sv.getDeleted() == 1) {
            throw new NotFoundException("秒杀券不存在");
        }

        long now = System.currentTimeMillis();
        Integer remainStock = seckillRedisSupport.getStockFromRedis(voucherId, sv.getStock());

        SeckillVoucherDetailVO seckillVoucherDetailVO = new SeckillVoucherDetailVO();
        seckillVoucherDetailVO.setVoucherId(voucher.getId());
        seckillVoucherDetailVO.setShopId(voucher.getShopId());
        seckillVoucherDetailVO.setTitle(voucher.getTitle());
        seckillVoucherDetailVO.setSubTitle(voucher.getSubTitle());
        seckillVoucherDetailVO.setRules(voucher.getRules());
        seckillVoucherDetailVO.setPayValue(voucher.getPayValue());
        seckillVoucherDetailVO.setActualValue(voucher.getActualValue());
        seckillVoucherDetailVO.setTotalStock(sv.getTotalStock());
        seckillVoucherDetailVO.setRemainStock(remainStock);
        seckillVoucherDetailVO.setStockPercent(seckillStatusSupport.calculateStockPercent(remainStock, sv.getTotalStock()));
        seckillVoucherDetailVO.setBeginTime(seckillStatusSupport.toTimestamp(sv.getBeginTime()));
        seckillVoucherDetailVO.setEndTime(seckillStatusSupport.toTimestamp(sv.getEndTime()));
        seckillVoucherDetailVO.setValidBeginTime(seckillStatusSupport.toTimestamp(voucher.getValidBeginTime()));
        seckillVoucherDetailVO.setValidEndTime(seckillStatusSupport.toTimestamp(voucher.getValidEndTime()));

        int status = seckillStatusSupport.getSeckillStatus(sv.getBeginTime(), sv.getEndTime(), now, remainStock);
        seckillVoucherDetailVO.setSeckillStatus(status);
        seckillVoucherDetailVO.setSeckillStatusDesc(seckillStatusSupport.getSeckillStatusDesc(status));

        seckillShopSupport.fillShopInfo(seckillVoucherDetailVO, seckillShopSupport.getShopById(voucher.getShopId()));

        // 判断用户是否已购买
        if (userId != null) {
            seckillVoucherDetailVO.setHasBought(hasBought(voucherId, userId));
        } else {
            seckillVoucherDetailVO.setHasBought(false);
        }

        seckillVoucherDetailVO.setServerTime(now);
        return seckillVoucherDetailVO;
    }

    /**
     * 执行秒杀下单（核心业务）
     * <p>
     * 流程：
     * 1. 防重复点击（Redis 分布式锁）
     * 2. 校验优惠券、秒杀券存在性及秒杀时间
     * 3. 执行 Lua 脚本进行原子性检查（库存、限购）
     * 4. 如果 Lua 脚本返回成功，则扣减数据库库存并创建订单
     * 5. 若数据库扣减失败（超卖），则加入补偿队列
     * </p>
     *
     * @param dto    秒杀订单 DTO（包含秒杀券 ID）
     * @param userId 当前用户 ID
     * @return 订单信息 VO
     * @throws NotFoundException    优惠券或秒杀券不存在
     * @throws BadRequestException  参数校验失败、重复点击、库存不足、限购、秒杀未开始/已结束等
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillOrderVO doSeckill(SeckillOrderDTO dto, Long userId) {
        Long voucherId = dto.getVoucherId();

        // 防重复点击（使用 Redis 锁，TTL 5秒）
        if (!seckillRedisSupport.tryAcquireButtonLock(userId, voucherId)) {
            throw new BadRequestException("请勿重复点击");
        }

        // 校验优惠券
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || voucher.getDeleted() == 1) {
            throw new NotFoundException("优惠券不存在");
        }

        SeckillVoucher sv = seckillVoucherMapper.selectById(voucherId);
        if (sv == null || sv.getDeleted() == 1) {
            throw new NotFoundException("秒杀券不存在");
        }

        // 校验秒杀时间
        long now = System.currentTimeMillis();
        if (now < seckillStatusSupport.toTimestamp(sv.getBeginTime())) {
            throw new BadRequestException("秒杀尚未开始");
        }
        if (now > seckillStatusSupport.toTimestamp(sv.getEndTime())) {
            throw new BadRequestException("秒杀已结束");
        }

        // 执行 Lua 脚本（原子性检查库存、限购）
        Long result = stringRedisTemplate.execute(
                seckillRedisScript,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );

        if (result == null || result != 1) {
            if (result == -1) {
                throw new BadRequestException("库存不足");
            } else if (result == -2) {
                throw new BadRequestException("秒杀数据未预热，请稍后重试");
            } else if (result == -3) {
                throw new BadRequestException("每人限购一份");
            }
            throw new BadRequestException("秒杀失败，请重试");
        }

        // 扣减数据库库存（防止超卖）
        int updated = seckillVoucherMapper.decrStock(voucherId);
        if (updated <= 0) {
            log.error("数据库库存扣减失败，可能已超卖！voucherId={}", voucherId);
            seckillCompensationSupport.enqueueRetry(voucherId, userId, false);
            throw new BadRequestException("库存不足，秒杀失败");
        }

        try {
            VoucherOrder order = new VoucherOrder();
            order.setVoucherId(voucherId);
            order.setUserId(userId);
            order.setOrderNo(generateOrderNo());
            order.setStatus(0);
            order.setPayType(0);
            order.setCreateTime(LocalDateTime.now());
            order.setDeleted(0);

            voucherOrderMapper.insert(order);
            seckillRedisSupport.cacheOrderResult(userId, voucherId, order.getId());

            SeckillOrderVO vo = new SeckillOrderVO();
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            return vo;
        } catch (Exception e) {
            log.error("秒杀订单落库失败，进入补偿队列，voucherId={}, userId={}", voucherId, userId, e);
            seckillCompensationSupport.enqueueRetry(voucherId, userId, true);
            throw new BadRequestException("秒杀请求处理中，请稍后查看订单结果");
        }
    }

    /**
     * 获取订单结果详情
     *
     * @param orderId 订单 ID
     * @param userId  用户 ID
     * @return 订单结果 VO
     * @throws NotFoundException   订单不存在
     * @throws BadRequestException 用户无权查看该订单
     */
    @Override
    public SeckillOrderResultVO getOrderResult(Long orderId, Long userId) {
        VoucherOrder order = voucherOrderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            throw new NotFoundException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("无权查看此订单");
        }

        Voucher voucher = voucherMapper.selectById(order.getVoucherId());

        SeckillOrderResultVO vo = new SeckillOrderResultVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setVoucherId(order.getVoucherId());
        vo.setTitle(voucher != null ? voucher.getTitle() : null);
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(seckillStatusSupport.getOrderStatusDesc(order.getStatus()));
        vo.setCreateTime(seckillStatusSupport.toTimestamp(order.getCreateTime()));

        // 已取消的订单不设置支付截止时间
        if (order.getStatus() == -1) {
            vo.setPayDeadline(null);
            return vo;
        }

        // 待支付订单设置15分钟支付截止时间
        if (order.getStatus() == 0 && order.getCreateTime() != null) {
            LocalDateTime deadline = order.getCreateTime().plusMinutes(15);
            vo.setPayDeadline(seckillStatusSupport.toTimestamp(deadline));
        }

        return vo;
    }

    // ================== 私有辅助方法 ==================

    /**
     * 判断用户是否已经购买过指定秒杀券
     *
     * @param voucherId 秒杀券 ID
     * @param userId    用户 ID
     * @return true 已购买，false 未购买
     */
    private boolean hasBought(Long voucherId, Long userId) {
        if (seckillRedisSupport.hasBought(voucherId, userId)) {
            return true;
        }

        int count = voucherOrderMapper.countByVoucherIdAndUserId(voucherId, userId);
        if (count > 0) {
            seckillRedisSupport.markBought(voucherId, userId);
            return true;
        }

        return false;
    }

    /**
     * 生成订单号
     *
     * @return 订单号（前缀 SK + 雪花ID）
     */
    private String generateOrderNo() {
        return "SK" + snowflakeIdGenerator.nextId();
    }
}
