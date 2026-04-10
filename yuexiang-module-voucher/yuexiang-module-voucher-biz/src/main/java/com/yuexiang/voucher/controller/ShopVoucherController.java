package com.yuexiang.voucher.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.voucher.domain.dto.VoucherCreateDTO;
import com.yuexiang.voucher.domain.vo.VoucherDetailVO;
import com.yuexiang.voucher.domain.vo.VoucherOrderVO;
import com.yuexiang.voucher.service.ShopVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商户端-优惠券管理")
@RestController
@RequestMapping("/api/shop/voucher")
@RequiredArgsConstructor
public class ShopVoucherController {

    private final ShopVoucherService shopVoucherService;

    @Operation(summary = "创建优惠券")
    @PostMapping
    public CommonResult<Long> createVoucher(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @RequestBody VoucherCreateDTO dto) {
        return CommonResult.success(shopVoucherService.createVoucher(shopId, dto));
    }

    @Operation(summary = "修改优惠券")
    @PutMapping("/{id}")
    public CommonResult<Boolean> updateVoucher(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @Parameter(description = "优惠券ID") @PathVariable("id") Long voucherId,
            @RequestBody VoucherCreateDTO dto) {
        return CommonResult.success(shopVoucherService.updateVoucher(shopId, voucherId, dto));
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    public CommonResult<Boolean> deleteVoucher(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @Parameter(description = "优惠券ID") @PathVariable("id") Long voucherId) {
        return CommonResult.success(shopVoucherService.deleteVoucher(shopId, voucherId));
    }

    @Operation(summary = "商户优惠券列表")
    @GetMapping
    public CommonResult<PageResult<VoucherDetailVO>> getVoucherList(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @Parameter(description = "券类型") @RequestParam(value = "type", required = false) Integer type,
            @Parameter(description = "状态") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return CommonResult.success(shopVoucherService.getShopVoucherList(shopId, type, status, pageNo, pageSize));
    }

    @Operation(summary = "上架优惠券")
    @PostMapping("/{id}/online")
    public CommonResult<Boolean> onlineVoucher(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @Parameter(description = "优惠券ID") @PathVariable("id") Long voucherId) {
        return CommonResult.success(shopVoucherService.onlineVoucher(shopId, voucherId));
    }

    @Operation(summary = "下架优惠券")
    @PostMapping("/{id}/offline")
    public CommonResult<Boolean> offlineVoucher(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @Parameter(description = "优惠券ID") @PathVariable("id") Long voucherId) {
        return CommonResult.success(shopVoucherService.offlineVoucher(shopId, voucherId));
    }

    @Operation(summary = "商户订单列表")
    @GetMapping("/order")
    public CommonResult<PageResult<VoucherOrderVO>> getOrderList(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @Parameter(description = "订单状态") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return CommonResult.success(shopVoucherService.getShopOrderList(shopId, status, pageNo, pageSize));
    }

    @Operation(summary = "核销优惠券")
    @PostMapping("/order/verify")
    public CommonResult<Boolean> verifyOrder(
            @Parameter(description = "商户ID") @RequestHeader("X-Shop-Id") Long shopId,
            @Parameter(description = "订单编号") @RequestParam(value = "orderNo") String orderNo) {
        return CommonResult.success(shopVoucherService.verifyOrder(shopId, orderNo));
    }
}
