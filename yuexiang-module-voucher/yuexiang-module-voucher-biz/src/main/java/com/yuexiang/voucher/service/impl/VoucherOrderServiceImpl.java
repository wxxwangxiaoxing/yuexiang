package com.yuexiang.voucher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.common.exception.ServiceUnavailableException;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.user.api.UserPointsService;
import com.yuexiang.user.api.UserWalletService;
import com.yuexiang.voucher.domain.entity.PaymentRecord;
import com.yuexiang.voucher.domain.entity.RefundRecord;
import com.yuexiang.voucher.domain.entity.Voucher;
import com.yuexiang.voucher.domain.entity.VoucherOrder;
import com.yuexiang.voucher.domain.vo.PaymentRecordVO;
import com.yuexiang.voucher.domain.vo.RefundRecordVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;
import com.yuexiang.voucher.mapper.PaymentRecordMapper;
import com.yuexiang.voucher.mapper.RefundRecordMapper;
import com.yuexiang.voucher.mapper.VoucherMapper;
import com.yuexiang.voucher.mapper.VoucherOrderMapper;
import com.yuexiang.voucher.service.VoucherOrderService;
import com.yuexiang.voucher.support.VoucherConverter;
import com.yuexiang.voucher.support.VoucherOrderRecordSupport;
import com.yuexiang.voucher.support.VoucherOrderStateSupport;
import com.yuexiang.voucher.support.VoucherOrderStockSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherOrderServiceImpl implements VoucherOrderService {

    private final VoucherMapper voucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final VoucherConverter voucherConverter;
    private final PaymentRecordMapper paymentRecordMapper;
    private final RefundRecordMapper refundRecordMapper;
    private final UserWalletService userWalletService;
    private final UserPointsService userPointsService;
    private final VoucherOrderStateSupport voucherOrderStateSupport;
    private final VoucherOrderRecordSupport voucherOrderRecordSupport;
    private final VoucherOrderStockSupport voucherOrderStockSupport;

    @Override
    public VoucherOrderVO getOrderDetail(Long orderId, Long userId) {
        VoucherOrder order = voucherOrderStateSupport.getOrder(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return null;
        }
        return voucherConverter.toOrderVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, Long userId, Integer payType) {
        VoucherOrder order = voucherOrderStateSupport.getOwnedOrder(orderId, userId);
        voucherOrderStateSupport.requireStatus(order, 0, "订单状态异常");

        Voucher voucher = voucherMapper.selectById(order.getVoucherId());
        if (voucher == null) {
            throw new NotFoundException("优惠券不存在");
        }

        if (payType == 1) {
            userWalletService.deductBalance(userId, voucher.getPayValue());
        }

        int rows = voucherOrderStateSupport.markPaid(orderId, payType, LocalDateTime.now());
        if (rows > 0) {
            int points = (int) (voucher.getPayValue() / 100);
            if (points > 0) {
                userPointsService.addPoints(userId, points, "VOUCHER_BUY", orderId, "购买优惠券赠送积分");
            }

            voucherOrderRecordSupport.createPaymentRecord(
                    userId,
                    orderId,
                    voucher.getPayValue().longValue(),
                    payType,
                    null,
                    "购买优惠券"
            );

            log.info("订单支付成功: orderId={}, userId={}, payType={}", orderId, userId, payType);
            return true;
        }

        throw new ServiceUnavailableException("支付失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId, Long userId) {
        VoucherOrder order = voucherOrderStateSupport.getOwnedOrder(orderId, userId);
        voucherOrderStateSupport.requireStatus(order, 0, "只能取消未支付的订单");

        int rows = voucherOrderStateSupport.markCancelled(orderId, 0);
        if (rows > 0) {
            voucherOrderStockSupport.restoreStock(order.getVoucherId(), userId);
            log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
            return true;
        }

        throw new ServiceUnavailableException("取消失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean useRefund(Long orderId, Long userId, String reason) {
        VoucherOrder order = voucherOrderStateSupport.getOwnedOrder(orderId, userId);
        voucherOrderStateSupport.requireStatus(order, 1, "只能退款已支付的订单");

        Voucher voucher = voucherMapper.selectById(order.getVoucherId());

        int rows = voucherOrderStateSupport.markRefunded(orderId, LocalDateTime.now());
        if (rows > 0) {
            if (order.getPayType() == 1 && voucher != null) {
                userWalletService.refundBalance(userId, voucher.getPayValue());
            }

            if (voucher != null) {
                int points = (int) (voucher.getPayValue() / 100);
                if (points > 0) {
                    userPointsService.deductPoints(userId, points, "VOUCHER_REFUND", orderId, "退款扣减积分");
                }
            }

            voucherOrderStockSupport.restoreStock(order.getVoucherId(), userId);

            voucherOrderRecordSupport.createRefundRecord(
                    userId,
                    orderId,
                    voucher != null ? voucher.getPayValue() : 0L,
                    reason
            );

            log.info("退款成功: orderId={}, userId={}, reason={}", orderId, userId, reason);
            return true;
        }

        throw new ServiceUnavailableException("退款失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePayCallback(String thirdPaymentNo, Long orderId, Integer payType) {
        VoucherOrder order = voucherOrderStateSupport.getOrder(orderId);
        if (order == null || order.getStatus() != 0) {
            log.warn("支付回调处理失败，订单状态异常 orderId={}, status={}", orderId, order != null ? order.getStatus() : null);
            return false;
        }

        Voucher voucher = voucherMapper.selectById(order.getVoucherId());
        if (voucher == null) {
            log.error("支付回调处理失败，优惠券不存在 orderId={}", orderId);
            return false;
        }

        int rows = voucherOrderStateSupport.markPaid(orderId, payType, LocalDateTime.now());
        if (rows > 0) {
            int points = (int) (voucher.getPayValue() / 100);
            if (points > 0) {
                userPointsService.addPoints(order.getUserId(), points, "VOUCHER_BUY", orderId, "购买优惠券赠送积分");
            }

            voucherOrderRecordSupport.createPaymentRecord(
                    order.getUserId(),
                    orderId,
                    voucher.getPayValue().longValue(),
                    payType,
                    thirdPaymentNo,
                    "第三方支付回调"
            );

            log.info("支付回调处理成功: orderId={}, thirdPaymentNo={}", orderId, thirdPaymentNo);
            return true;
        }
        log.warn("支付回调处理失败，订单可能已被其他线程处理 orderId={}", orderId);
        return false;
    }

    @Override
    public int cancelTimeoutOrders() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(15);
        List<VoucherOrder> timeoutOrders = voucherOrderStateSupport.listTimeoutOrders(timeoutThreshold);
        if (timeoutOrders.isEmpty()) {
            return 0;
        }

        int cancelled = 0;
        for (VoucherOrder order : timeoutOrders) {
            try {
                int rows = voucherOrderStateSupport.markCancelled(order.getId(), 0);
                if (rows > 0) {
                    voucherOrderStockSupport.restoreStock(order.getVoucherId(), order.getUserId());
                    cancelled++;
                    log.info("订单超时自动取消: orderId={}, userId={}", order.getId(), order.getUserId());
                }
            } catch (Exception e) {
                log.error("取消超时订单失败 orderId={}", order.getId(), e);
            }
        }
        return cancelled;
    }

    @Override
    public PageResult<VoucherOrderVO> getMyOrders(Long userId, Integer status, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoucherOrder::getUserId, userId)
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
    public PageResult<PaymentRecordVO> getPaymentRecords(Long userId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getUserId, userId)
                .orderByDesc(PaymentRecord::getCreateTime);

        Page<PaymentRecord> page = paymentRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

        List<PaymentRecordVO> records = page.getRecords().stream()
                .map(this::convertToPaymentRecordVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal());
    }

    @Override
    public PageResult<RefundRecordVO> getRefundRecords(Long userId, Integer status, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<RefundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundRecord::getUserId, userId)
                .eq(status != null, RefundRecord::getStatus, status)
                .orderByDesc(RefundRecord::getCreateTime);

        Page<RefundRecord> page = refundRecordMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

        List<RefundRecordVO> records = page.getRecords().stream()
                .map(this::convertToRefundRecordVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal());
    }

    private PaymentRecordVO convertToPaymentRecordVO(PaymentRecord record) {
        PaymentRecordVO vo = new PaymentRecordVO();
        vo.setId(record.getId());
        vo.setPaymentNo(record.getPaymentNo());
        vo.setThirdPaymentNo(record.getThirdPaymentNo());
        vo.setOrderId(record.getOrderId());
        vo.setAmount(record.getAmount());
        vo.setPayType(record.getPayType());
        vo.setPayTypeDesc(getPayTypeDesc(record.getPayType()));
        vo.setStatus(record.getStatus());
        vo.setStatusDesc(getPaymentStatusDesc(record.getStatus()));
        vo.setCreateTime(record.getCreateTime());
        vo.setFinishTime(record.getFinishTime());
        return vo;
    }

    private RefundRecordVO convertToRefundRecordVO(RefundRecord record) {
        RefundRecordVO vo = new RefundRecordVO();
        vo.setId(record.getId());
        vo.setRefundNo(record.getRefundNo());
        vo.setOrderId(record.getOrderId());
        vo.setRefundAmount(record.getRefundAmount());
        vo.setReason(record.getReason());
        vo.setStatus(record.getStatus());
        vo.setStatusDesc(getRefundStatusDesc(record.getStatus()));
        vo.setRejectReason(record.getRejectReason());
        vo.setCreateTime(record.getCreateTime());
        vo.setRefundTime(record.getRefundTime());
        return vo;
    }

    private String getPayTypeDesc(Integer payType) {
        if (payType == null) return "未知";
        return switch (payType) {
            case 1 -> "余额";
            case 2 -> "微信";
            case 3 -> "支付宝";
            default -> "未知";
        };
    }

    private String getPaymentStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "处理中";
            case 1 -> "成功";
            case 2 -> "失败";
            default -> "未知";
        };
    }

    private String getRefundStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "申请中";
            case 1 -> "审核通过";
            case 2 -> "退款成功";
            case 3 -> "已拒绝";
            default -> "未知";
        };
    }
}
