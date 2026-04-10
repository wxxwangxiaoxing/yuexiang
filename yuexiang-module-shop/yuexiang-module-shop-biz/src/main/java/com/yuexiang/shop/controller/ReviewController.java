package com.yuexiang.shop.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.shop.domain.dto.ReviewCreateDTO;
import com.yuexiang.shop.domain.vo.ReviewSummaryVO;
import com.yuexiang.shop.domain.vo.ReviewVO;
import com.yuexiang.shop.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商户评价接口")
@RestController
@RequestMapping("/api/shop/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "商户评价列表")
    @GetMapping("/list")
    public CommonResult<PageResult<ReviewVO>> getShopReviews(
            @Parameter(description = "商户ID") @RequestParam(value = "shopId") Long shopId,
            @Parameter(description = "排序：0最新，1最热") @RequestParam(value = "sortBy", defaultValue = "0") Integer sortBy,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(reviewService.getShopReviews(shopId, sortBy, pageNo, pageSize, userId));
    }

    @Operation(summary = "评价统计摘要")
    @GetMapping("/summary")
    public CommonResult<ReviewSummaryVO> getReviewSummary(
            @Parameter(description = "商户ID") @RequestParam(value = "shopId") Long shopId) {
        return CommonResult.success(reviewService.getReviewSummary(shopId));
    }

    @Operation(summary = "提交评价")
    @PostMapping("/create")
    public CommonResult<Void> createReview(
            @Valid @RequestBody ReviewCreateDTO dto) {
        Long userId = UserContext.getUserId();
        reviewService.createReview(dto, userId);
        return CommonResult.success(null);
    }
}
