package com.yuexiang.voucher.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.voucher.domain.vo.PaymentRecordVO;
import com.yuexiang.voucher.domain.vo.RefundRecordVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;
import com.yuexiang.voucher.service.VoucherOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "优惠券订单接口")
@RestController
@RequestMapping("/api/voucher-order")
@RequiredArgsConstructor
public class VoucherOrderController {

    private final VoucherOrderService voucherOrderService;

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public CommonResult<VoucherOrderVO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherOrderService.getOrderDetail(orderId, userId));
    }

    @Operation(summary = "支付订单")
    @PostMapping("/{id}/pay")
    public CommonResult<Boolean> payOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId,
            @Parameter(description = "支付方式：1余额，2微信，3支付宝") @RequestParam(value = "payType") Integer payType) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherOrderService.payOrder(orderId, userId, payType));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public CommonResult<Boolean> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherOrderService.cancelOrder(orderId, userId));
    }

    @Operation(summary = "申请退款")
    @PostMapping("/{id}/refund")
    public CommonResult<Boolean> refundOrder(
            @Parameter(description = "订单ID") @PathVariable("id") Long orderId,
            @Parameter(description = "退款原因") @RequestParam(value = "reason", required = false) String reason) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherOrderService.useRefund(orderId, userId, reason));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/my")
    public CommonResult<PageResult<VoucherOrderVO>> getMyOrders(
            @Parameter(description = "状态：0未支付，1已支付，2已使用，3已退款，4已取消") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherOrderService.getMyOrders(userId, status, pageNo, pageSize));
    }

    @Operation(summary = "支付流水记录")
    @GetMapping("/payments")
    public CommonResult<PageResult<PaymentRecordVO>> getPaymentRecords(
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherOrderService.getPaymentRecords(userId, pageNo, pageSize));
    }

    @Operation(summary = "退款记录")
    @GetMapping("/refunds")
    public CommonResult<PageResult<RefundRecordVO>> getRefundRecords(
            @Parameter(description = "状态：0申请中，1审核通过，2退款成功，3已拒绝") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherOrderService.getRefundRecords(userId, status, pageNo, pageSize));
    }
}
