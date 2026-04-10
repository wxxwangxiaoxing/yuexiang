package com.yuexiang.voucher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.voucher.constant.VoucherStatusConstants;
import com.yuexiang.voucher.domain.entity.Voucher;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import com.yuexiang.voucher.domain.vo.VoucherDetailVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;
import com.yuexiang.voucher.mapper.VoucherMapper;
import com.yuexiang.voucher.mapper.VoucherOrderMapper;
import com.yuexiang.voucher.service.VoucherService;
import com.yuexiang.voucher.support.VoucherConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券业务实现类
 * <p>
 * 主要功能：
 * 1. 查询优惠券详情
 * 2. 分页查询店铺优惠券
 * 3. 分页查询用户已领取的优惠券
 * 4. 分页查询用户当前可用的优惠券
 * 5. 查询用户即将过期的优惠券
 */
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherMapper voucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final VoucherConverter voucherConverter;

    /**
     * 查询优惠券详情
     *
     * @param voucherId 优惠券ID
     * @return 优惠券详情VO，如果不存在则返回 null
     */
    @Override
    public VoucherDetailVO getVoucherDetail(Long voucherId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        return voucher == null ? null : voucherConverter.toDetailVO(voucher);
    }

    /**
     * 分页查询店铺优惠券列表
     * <p>
     * 查询条件：
     * 1. 指定店铺ID
     * 2. 优惠券状态为正常
     * 3. 未被逻辑删除
     * 4. 按创建时间倒序排列
     *
     * @param shopId 店铺ID
     * @param pageNo 当前页
     * @param pageSize 每页大小
     * @return 分页后的优惠券详情列表
     */
    @Override
    public PageResult<VoucherDetailVO> getShopVouchers(Long shopId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getShopId, shopId)
                .eq(Voucher::getStatus, VoucherStatusConstants.VOUCHER_STATUS_NORMAL)
                .eq(Voucher::getDeleted, VoucherStatusConstants.NOT_DELETED)
                .orderByDesc(Voucher::getCreateTime);

        // 分页查询优惠券数据
        Page<Voucher> page = voucherMapper.selectPage(buildPage(pageNo, pageSize), wrapper);

        // 实体对象转换为 VO
        List<VoucherDetailVO> records = page.getRecords().stream()
                .map(voucherConverter::toDetailVO)
                .toList();

        return new PageResult<>(records, page.getTotal());
    }

    /**
     * 分页查询用户已领取的优惠券
     * <p>
     * 查询条件：
     * 1. 指定用户ID
     * 2. 如果传入状态则按状态筛选
     * 3. 未被逻辑删除
     * 4. 按创建时间倒序排列
     *
     * @param userId 用户ID
     * @param status 优惠券订单状态，可为空
     * @param pageNo 当前页
     * @param pageSize 每页大小
     * @return 分页后的用户优惠券订单列表
     */
    @Override
    public PageResult<VoucherOrderVO> getMyVouchers(Long userId, Integer status, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .eq(status != null, VoucherOrder::getStatus, status)
                .eq(VoucherOrder::getDeleted, VoucherStatusConstants.NOT_DELETED)
                .orderByDesc(VoucherOrder::getCreateTime);

        // 分页查询用户优惠券订单
        Page<VoucherOrder> page = voucherOrderMapper.selectPage(buildPage(pageNo, pageSize), wrapper);

        // 转换为前端展示对象
        List<VoucherOrderVO> records = page.getRecords().stream()
                .map(voucherConverter::toOrderVO)
                .toList();

        return new PageResult<>(records, page.getTotal());
    }

    /**
     * 分页查询用户可用优惠券
     * <p>
     * 查询条件：
     * 1. 指定用户ID
     * 2. 优惠券订单状态为可使用
     * 3. 未被逻辑删除
     * 4. 如果传入店铺ID，则进一步筛选该店铺下的优惠券
     * <p>
     * 说明：
     * - 先查出店铺对应的优惠券ID集合
     * - 再根据 voucherId IN (...) 查询用户拥有的可用券
     *
     * @param userId 用户ID
     * @param shopId 店铺ID，可为空
     * @param pageNo 当前页
     * @param pageSize 每页大小
     * @return 分页后的可用优惠券列表
     */
    @Override
    public PageResult<VoucherOrderVO> getAvailableVouchers(Long userId, Long shopId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getStatus, VoucherStatusConstants.ORDER_STATUS_AVAILABLE)
                .eq(VoucherOrder::getDeleted, VoucherStatusConstants.NOT_DELETED);

        // 如果指定了店铺，则先查询该店铺下的所有优惠券ID
        if (shopId != null) {
            List<Long> voucherIds = getVoucherIdsByShopId(shopId);

            // 店铺下没有优惠券，直接返回空结果，避免无意义查询
            if (CollectionUtils.isEmpty(voucherIds)) {
                return new PageResult<>(List.of(), 0L);
            }

            // 只查询这些优惠券ID对应的用户优惠券订单
            wrapper.in(VoucherOrder::getVoucherId, voucherIds);
        }

        wrapper.orderByDesc(VoucherOrder::getCreateTime);

        // 分页查询
        Page<VoucherOrder> page = voucherOrderMapper.selectPage(buildPage(pageNo, pageSize), wrapper);

        // 转换为 VO
        List<VoucherOrderVO> records = page.getRecords().stream()
                .map(voucherConverter::toOrderVO)
                .toList();

        return new PageResult<>(records, page.getTotal());
    }

    /**
     * 查询用户即将过期的优惠券
     * <p>
     * 查询逻辑：
     * 1. 先查出在指定天数内即将过期的优惠券ID
     * 2. 再查询该用户持有且状态为可用的优惠券订单
     * <p>
     * 例如：
     * days = 3，则查询从当前时间到未来3天内到期的优惠券
     *
     * @param userId 用户ID
     * @param days 未来多少天内即将过期
     * @return 即将过期的优惠券订单列表
     */
    @Override
    public List<VoucherOrderVO> getExpiringSoonVouchers(Long userId, int days) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 到期截止时间：当前时间 + days 天
        LocalDateTime deadline = now.plusDays(days);

        // 查询在指定时间范围内即将过期的优惠券ID
        List<Long> expiringVoucherIds = voucherMapper.selectList(
                new LambdaQueryWrapper<Voucher>()
                        .eq(Voucher::getStatus, VoucherStatusConstants.VOUCHER_STATUS_NORMAL)
                        .eq(Voucher::getDeleted, VoucherStatusConstants.NOT_DELETED)
                        .isNotNull(Voucher::getValidEndTime)
                        .between(Voucher::getValidEndTime, now, deadline)
                        .select(Voucher::getId)
        ).stream().map(Voucher::getId).toList();

        // 没有即将过期的优惠券，直接返回空列表
        if (CollectionUtils.isEmpty(expiringVoucherIds)) {
            return List.of();
        }

        // 查询该用户持有的、状态可用的、且属于即将过期券ID集合中的优惠券订单
        List<VoucherOrder> orders = voucherOrderMapper.selectList(
                new LambdaQueryWrapper<VoucherOrder>()
                        .eq(VoucherOrder::getUserId, userId)
                        .eq(VoucherOrder::getStatus, VoucherStatusConstants.ORDER_STATUS_AVAILABLE)
                        .eq(VoucherOrder::getDeleted, VoucherStatusConstants.NOT_DELETED)
                        .in(VoucherOrder::getVoucherId, expiringVoucherIds)
                        .orderByAsc(VoucherOrder::getCreateTime)
        );

        // 转换为 VO 返回
        return orders.stream()
                .map(voucherConverter::toOrderVO)
                .toList();
    }

    /**
     * 构建 MyBatis-Plus 分页对象
     *
     * @param pageNo 当前页
     * @param pageSize 每页大小
     * @param <T> 分页数据类型
     * @return 分页对象
     */
    private <T> Page<T> buildPage(Integer pageNo, Integer pageSize) {
        return new Page<>(pageNo, pageSize);
    }

    /**
     * 根据店铺ID查询该店铺下所有优惠券ID
     * <p>
     * 查询条件：
     * 1. 店铺ID匹配
     * 2. 未被逻辑删除
     * <p>
     * 说明：
     * 这里只查询 id 字段，减少数据库返回的数据量
     *
     * @param shopId 店铺ID
     * @return 优惠券ID列表
     */
    private List<Long> getVoucherIdsByShopId(Long shopId) {
        return voucherMapper.selectList(
                new LambdaQueryWrapper<Voucher>()
                        .eq(Voucher::getShopId, shopId)
                        .eq(Voucher::getDeleted, VoucherStatusConstants.NOT_DELETED)
                        .select(Voucher::getId)
        ).stream().map(Voucher::getId).toList();
    }
}
