package com.yuexiang.user.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.user.api.UserCenterService;
import com.yuexiang.user.domain.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserCenterController {

    private final UserCenterService userCenterService;

    @Operation(summary = "我的优惠券")
    @GetMapping("/vouchers")
    public CommonResult<PageResult<MyVoucherVO>> getMyVouchers(
            @Parameter(description = "状态：1未使用 2已使用 3已过期") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userCenterService.getMyVouchers(userId, status, pageNo, pageSize));
    }

    @Operation(summary = "我的订单")
    @GetMapping("/orders")
    public CommonResult<PageResult<MyOrderVO>> getMyOrders(
            @Parameter(description = "状态：0待支付 1已支付 2已使用 3已退款") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userCenterService.getMyOrders(userId, status, pageNo, pageSize));
    }

    @Operation(summary = "我的笔记")
    @GetMapping("/blogs")
    public CommonResult<PageResult<MyBlogVO>> getMyBlogs(
            @Parameter(description = "状态：0待审核 1已发布 2已屏蔽 3草稿") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userCenterService.getMyBlogs(userId, status, pageNo, pageSize));
    }

    @Operation(summary = "我的收藏")
    @GetMapping("/favorites")
    public CommonResult<PageResult<MyFavoriteVO>> getMyFavorites(
            @Parameter(description = "类型：1商户 2笔记") @RequestParam(value = "type", required = false) Integer type,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userCenterService.getMyFavorites(userId, type, pageNo, pageSize));
    }

    @Operation(summary = "浏览足迹")
    @GetMapping("/history")
    public CommonResult<BrowseHistoryPageVO> getBrowseHistory(
            @Parameter(description = "类型：1商户 2笔记") @RequestParam(value = "type", required = false) Integer type,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userCenterService.getBrowseHistory(userId, type, pageNo, pageSize));
    }

    @Operation(summary = "清空浏览足迹")
    @DeleteMapping("/history")
    public CommonResult<Integer> clearBrowseHistory(
            @Parameter(description = "类型：1商户 2笔记，不传则清空全部") @RequestParam(value = "type", required = false) Integer type) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userCenterService.clearBrowseHistory(userId, type));
    }

    @Operation(summary = "AI探店记录")
    @GetMapping("/ai-records")
    public CommonResult<PageResult<AiRecordVO>> getAiRecords(
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(userCenterService.getAiRecords(userId, pageNo, pageSize));
    }
}
