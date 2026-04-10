package com.yuexiang.voucher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.common.exception.ForbiddenException;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.common.exception.ServiceUnavailableException;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.voucher.constants.SeckillRedisConstants;
import com.yuexiang.voucher.domain.dto.VoucherCreateDTO;
import com.yuexiang.voucher.domain.entity.SeckillVoucher;
import com.yuexiang.voucher.domain.entity.Voucher;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import com.yuexiang.voucher.domain.vo.VoucherDetailVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;
import com.yuexiang.voucher.mapper.SeckillVoucherMapper;
import com.yuexiang.voucher.mapper.VoucherMapper;
import com.yuexiang.voucher.mapper.VoucherOrderMapper;
import com.yuexiang.voucher.service.SeckillCacheService;
import com.yuexiang.voucher.service.ShopVoucherService;
import com.yuexiang.voucher.support.VoucherConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopVoucherServiceImpl implements ShopVoucherService {

    private final VoucherMapper voucherMapper;
    private final SeckillVoucherMapper seckillVoucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillCacheService seckillCacheService;
    private final VoucherConverter voucherConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucher(Long shopId, VoucherCreateDTO dto) {
        Voucher voucher = new Voucher();
        voucher.setShopId(shopId);
        voucher.setTitle(dto.getTitle());
        voucher.setSubTitle(dto.getSubTitle());
        voucher.setRules(dto.getRules());
        voucher.setPayValue(dto.getPayValue());
        voucher.setActualValue(dto.getActualValue());
        voucher.setType(dto.getType());
        voucher.setStatus(1);
        voucher.setValidBeginTime(dto.getValidBeginTime());
        voucher.setValidEndTime(dto.getValidEndTime());

        voucherMapper.insert(voucher);

        if (dto.getType() == 1) {
            if (dto.getStock() == null || dto.getBeginTime() == null || dto.getEndTime() == null) {
                throw new BadRequestException("秒杀券必须填写库存和时间");
            }

            SeckillVoucher seckillVoucher = new SeckillVoucher();
            seckillVoucher.setVoucherId(voucher.getId());
            seckillVoucher.setTotalStock(dto.getStock());
            seckillVoucher.setStock(dto.getStock());
            seckillVoucher.setBeginTime(dto.getBeginTime());
            seckillVoucher.setEndTime(dto.getEndTime());

            seckillVoucherMapper.insert(seckillVoucher);

            stringRedisTemplate.opsForValue().set(
                    SeckillRedisConstants.SECKILL_STOCK_KEY + voucher.getId(),
                    String.valueOf(dto.getStock())
            );
        }

        log.info("商户创建优惠券成功: shopId={}, voucherId={}, type={}", shopId, voucher.getId(), dto.getType());
        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVoucher(Long shopId, Long voucherId, VoucherCreateDTO dto) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getShopId().equals(shopId)) {
            throw new NotFoundException("优惠券不存在");
        }

        voucher.setTitle(dto.getTitle());
        voucher.setSubTitle(dto.getSubTitle());
        voucher.setRules(dto.getRules());
        voucher.setPayValue(dto.getPayValue());
        voucher.setActualValue(dto.getActualValue());
        voucher.setValidBeginTime(dto.getValidBeginTime());
        voucher.setValidEndTime(dto.getValidEndTime());

        voucherMapper.updateById(voucher);

        if (voucher.getType() == 1 && dto.getStock() != null) {
            SeckillVoucher seckillVoucher = seckillVoucherMapper.selectById(voucherId);
            if (seckillVoucher != null) {
                seckillVoucher.setBeginTime(dto.getBeginTime());
                seckillVoucher.setEndTime(dto.getEndTime());
                seckillVoucherMapper.updateById(seckillVoucher);

                stringRedisTemplate.opsForValue().set(
                        SeckillRedisConstants.SECKILL_STOCK_KEY + voucherId,
                        String.valueOf(seckillVoucher.getStock())
                );
            }
        }

        seckillCacheService.evictListCache();
        log.info("商户更新优惠券成功: shopId={}, voucherId={}", shopId, voucherId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVoucher(Long shopId, Long voucherId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getShopId().equals(shopId)) {
            throw new NotFoundException("优惠券不存在");
        }

        if (voucher.getStatus() == 0) {
            throw new BadRequestException("上架中的优惠券不能删除");
        }

        voucherMapper.deleteById(voucherId);

        if (voucher.getType() == 1) {
            seckillVoucherMapper.deleteById(voucherId);
            stringRedisTemplate.delete(SeckillRedisConstants.SECKILL_STOCK_KEY + voucherId);
            stringRedisTemplate.delete(SeckillRedisConstants.SECKILL_ORDER_KEY + voucherId);
        }

        seckillCacheService.evictListCache();
        log.info("商户删除优惠券成功: shopId={}, voucherId={}", shopId, voucherId);
        return true;
    }

    @Override
    public PageResult<VoucherDetailVO> getShopVoucherList(Long shopId, Integer type, Integer status, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Voucher::getShopId, shopId)
                .eq(type != null, Voucher::getType, type)
                .eq(status != null, Voucher::getStatus, status)
                .eq(Voucher::getDeleted, 0)
                .orderByDesc(Voucher::getCreateTime);

        Page<Voucher> page = voucherMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

        List<VoucherDetailVO> records = page.getRecords().stream()
                .map(voucherConverter::toDetailVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean onlineVoucher(Long shopId, Long voucherId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getShopId().equals(shopId)) {
            throw new NotFoundException("优惠券不存在");
        }

        if (voucher.getStatus() == 0) {
            return true;
        }

        LambdaUpdateWrapper<Voucher> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Voucher::getId, voucherId)
                .set(Voucher::getStatus, 0);

        int rows = voucherMapper.update(null, updateWrapper);
        if (rows > 0) {
            seckillCacheService.evictListCache();
            log.info("商户上架优惠券成功: shopId={}, voucherId={}", shopId, voucherId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offlineVoucher(Long shopId, Long voucherId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null || !voucher.getShopId().equals(shopId)) {
            throw new NotFoundException("优惠券不存在");
        }

        if (voucher.getStatus() != 0) {
            return true;
        }

        LambdaUpdateWrapper<Voucher> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Voucher::getId, voucherId)
                .set(Voucher::getStatus, 1);

        int rows = voucherMapper.update(null, updateWrapper);
        if (rows > 0) {
            seckillCacheService.evictListCache();
            log.info("商户下架优惠券成功: shopId={}, voucherId={}", shopId, voucherId);
            return true;
        }

        return false;
    }

    @Override
    public PageResult<VoucherOrderVO> getShopOrderList(Long shopId, Integer status, Integer pageNo, Integer pageSize) {
        List<Long> voucherIds = voucherMapper.selectList(
                new LambdaQueryWrapper<Voucher>()
                        .eq(Voucher::getShopId, shopId)
                        .select(Voucher::getId)
        ).stream().map(Voucher::getId).toList();

        if (voucherIds.isEmpty()) {
            return new PageResult<>(List.of(), 0L);
        }

        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(VoucherOrder::getVoucherId, voucherIds)
                .eq(status != null, VoucherOrder::getStatus, status)
                .eq(VoucherOrder::getDeleted, 0)
                .orderByDesc(VoucherOrder::getCreateTime);

        Page<VoucherOrder> page = voucherOrderMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

        List<VoucherOrderVO> records = page.getRecords().stream()
                .map(voucherConverter::toOrderVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyOrder(Long shopId, String orderNo) {
        VoucherOrder order = voucherOrderMapper.selectOne(
                new LambdaQueryWrapper<VoucherOrder>()
                        .eq(VoucherOrder::getOrderNo, orderNo)
        );

        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        Voucher voucher = voucherMapper.selectById(order.getVoucherId());
        if (voucher == null || !voucher.getShopId().equals(shopId)) {
            throw new ForbiddenException("无权核销此订单");
        }

        if (order.getStatus() != 1) {
            throw new BusinessException(400, "订单状态异常，无法核销");
        }

        LambdaUpdateWrapper<VoucherOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(VoucherOrder::getId, order.getId())
                .eq(VoucherOrder::getStatus, 1)
                .set(VoucherOrder::getStatus, 2)
                .set(VoucherOrder::getUseTime, LocalDateTime.now());

        int rows = voucherOrderMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("商户核销订单成功: shopId={}, orderNo={}", shopId, orderNo);
            return true;
        }

        throw new ServiceUnavailableException("核销失败，请重试");
    }
}
