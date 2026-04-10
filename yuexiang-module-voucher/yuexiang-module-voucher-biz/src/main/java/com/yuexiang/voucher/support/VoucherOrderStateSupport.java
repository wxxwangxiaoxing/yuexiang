package com.yuexiang.voucher.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import com.yuexiang.voucher.mapper.VoucherOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VoucherOrderStateSupport {

    private final VoucherOrderMapper voucherOrderMapper;

    public VoucherOrder getOwnedOrder(Long orderId, Long userId) {
        VoucherOrder order = voucherOrderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new NotFoundException("订单不存在");
        }
        return order;
    }

    public VoucherOrder getOrder(Long orderId) {
        return voucherOrderMapper.selectById(orderId);
    }

    public void requireStatus(VoucherOrder order, int expectedStatus, String message) {
        if (order.getStatus() != expectedStatus) {
            throw new BadRequestException(message);
        }
    }

    public int markPaid(Long orderId, Integer payType, LocalDateTime payTime) {
        LambdaUpdateWrapper<VoucherOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(VoucherOrder::getId, orderId)
                .eq(VoucherOrder::getStatus, 0)
                .set(VoucherOrder::getStatus, 1)
                .set(VoucherOrder::getPayType, payType)
                .set(VoucherOrder::getPayTime, payTime);
        return voucherOrderMapper.update(null, updateWrapper);
    }

    public int markCancelled(Long orderId, int expectedStatus) {
        LambdaUpdateWrapper<VoucherOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(VoucherOrder::getId, orderId)
                .eq(VoucherOrder::getStatus, expectedStatus)
                .set(VoucherOrder::getStatus, 4);
        return voucherOrderMapper.update(null, updateWrapper);
    }

    public int markRefunded(Long orderId, LocalDateTime refundTime) {
        LambdaUpdateWrapper<VoucherOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(VoucherOrder::getId, orderId)
                .eq(VoucherOrder::getStatus, 1)
                .set(VoucherOrder::getStatus, 3)
                .set(VoucherOrder::getRefundTime, refundTime);
        return voucherOrderMapper.update(null, updateWrapper);
    }

    public List<VoucherOrder> listTimeoutOrders(LocalDateTime timeoutThreshold) {
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoucherOrder::getStatus, 0)
                .le(VoucherOrder::getCreateTime, timeoutThreshold)
                .eq(VoucherOrder::getDeleted, 0);
        return voucherOrderMapper.selectList(wrapper);
    }
}
