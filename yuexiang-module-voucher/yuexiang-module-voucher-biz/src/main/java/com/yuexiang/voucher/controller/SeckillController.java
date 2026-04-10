package com.yuexiang.voucher.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.voucher.domain.dto.SeckillOrderDTO;
import com.yuexiang.voucher.domain.vo.*;
import com.yuexiang.voucher.service.SeckillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "秒杀活动", description = "秒杀活动页相关接口")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "获取服务器时间", description = "获取服务器当前时间戳，用于客户端时间校准")
    @GetMapping("/time")
    public CommonResult<ServerTimeVO> getServerTime() {
        return CommonResult.success(seckillService.getServerTime());
    }

    @Operation(summary = "查询秒杀场次列表", description = "查询指定日期的秒杀场次列表")
    @GetMapping("/sessions")
    public CommonResult<SessionListVO> getSessions(
            @Parameter(description = "日期(yyyy-MM-dd)，默认今天")
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return CommonResult.success(seckillService.getSessions(date));
    }

    @Operation(summary = "查询场次秒杀券列表", description = "查询指定场次的秒杀券列表")
    @GetMapping("/session/{sessionId}/vouchers")
    public CommonResult<SeckillVoucherListVO> getVouchers(
            @PathVariable("sessionId") @Parameter(description = "场次ID") Long sessionId,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return CommonResult.success(seckillService.getVouchers(sessionId, page, pageSize));
    }

    @Operation(summary = "查询秒杀券详情", description = "查询秒杀券详细信息")
    @GetMapping("/voucher/{voucherId}")
    public CommonResult<SeckillVoucherDetailVO> getVoucherDetail(
            @Parameter(description = "秒杀券ID") @PathVariable("voucherId") Long voucherId) {
        Long userId = getCurrentUserId();
        return CommonResult.success(seckillService.getVoucherDetail(voucherId, userId));
    }

    @Operation(summary = "秒杀下单", description = "执行秒杀下单操作")
    @PostMapping("/order")
    public CommonResult<SeckillOrderVO> doSeckill(@Valid @RequestBody SeckillOrderDTO dto) {
        Long userId = getRequiredUserId();
        return CommonResult.success(seckillService.doSeckill(dto, userId));
    }

    @Operation(summary = "查询秒杀订单结果", description = "查询秒杀订单详情")
    @GetMapping("/order/{orderId}")
    public CommonResult<SeckillOrderResultVO> getOrderResult(
            @Parameter(description = "订单ID") @PathVariable("orderId") Long orderId) {
        Long userId = getRequiredUserId();
        return CommonResult.success(seckillService.getOrderResult(orderId, userId));
    }

    private Long getCurrentUserId() {
        LoginUser user = UserContext.get();
        return user != null ? user.getUserId() : null;
    }

    private Long getRequiredUserId() {
        return UserContext.getUserId();
    }
}
