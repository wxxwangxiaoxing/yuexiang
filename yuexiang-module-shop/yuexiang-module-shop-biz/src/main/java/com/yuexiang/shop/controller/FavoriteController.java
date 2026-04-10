package com.yuexiang.shop.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.shop.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "商户收藏接口")
@RestController
@RequestMapping("/api/shop/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "收藏/取消收藏商户")
    @PostMapping("/toggle")
    public CommonResult<Void> toggleFavorite(
            @Parameter(description = "商户ID") @RequestParam(value = "shopId") Long shopId) {
        Long userId = UserContext.getUserId();
        favoriteService.toggleFavorite(userId, shopId);
        return CommonResult.success(null);
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/is-favorite")
    public CommonResult<Boolean> isFavorite(
            @Parameter(description = "商户ID") @RequestParam(value = "shopId") Long shopId) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(favoriteService.isFavorite(userId, shopId));
    }

    @Operation(summary = "批量查询收藏状态")
    @PostMapping("/batch")
    public CommonResult<Set<Long>> batchCheckFavorite(
            @Parameter(description = "商户ID列表") @RequestBody Set<Long> shopIds) {
        Long userId = UserContext.getUserId();
        return CommonResult.success(favoriteService.getFavoriteShopIds(userId, shopIds));
    }
}
