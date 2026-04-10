package com.yuexiang.voucher.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.voucher.domain.vo.VoucherDetailVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;
import com.yuexiang.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "优惠券接口")
@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "获取优惠券详情")
    @GetMapping("/{id}")
    public CommonResult<VoucherDetailVO> getVoucherDetail(
            @Parameter(description = "优惠券ID") @PathVariable("id") Long voucherId) {
        return CommonResult.success(voucherService.getVoucherDetail(voucherId));
    }

    @Operation(summary = "获取商户优惠券列表")
    @GetMapping("/shop/{shopId}")
    public CommonResult<PageResult<VoucherDetailVO>> getShopVouchers(
            @PathVariable @Parameter(description = "商户ID") Long shopId,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return CommonResult.success(voucherService.getShopVouchers(shopId, pageNo, pageSize));
    }

    @Operation(summary = "我的优惠券列表")
    @GetMapping("/my")
    public CommonResult<PageResult<VoucherOrderVO>> getMyVouchers(
            @Parameter(description = "状态：0未支付，1已支付，2已使用，3已退款") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherService.getMyVouchers(userId, status, pageNo, pageSize));
    }

    @Operation(summary = "获取可用优惠券（下单时选择）")
    @GetMapping("/available")
    public CommonResult<PageResult<VoucherOrderVO>> getAvailableVouchers(
            @Parameter(description = "商户ID") @RequestParam(value = "shopId", required = false) Long shopId,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherService.getAvailableVouchers(userId, shopId, pageNo, pageSize));
    }

    @Operation(summary = "即将过期的优惠券")
    @GetMapping("/expiring-soon")
    public CommonResult<List<VoucherOrderVO>> getExpiringSoonVouchers(
            @Parameter(description = "天数，默认7天") @RequestParam(value = "days", defaultValue = "7") Integer days) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(voucherService.getExpiringSoonVouchers(userId, days));
    }
}
