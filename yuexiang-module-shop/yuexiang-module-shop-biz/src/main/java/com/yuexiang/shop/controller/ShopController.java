package com.yuexiang.shop.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;
import com.yuexiang.shop.domain.vo.NearbyShopPageVO;
import com.yuexiang.shop.domain.vo.ShopDetailVO;
import com.yuexiang.shop.service.NearbyShopQueryService;
import com.yuexiang.shop.service.ShopService;
import com.yuexiang.shop.support.GeoRateLimiter;
import com.yuexiang.shop.utils.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 */

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final NearbyShopQueryService nearbyShopQueryService;
    private final GeoRateLimiter geoRateLimiter;

    /**
     * 查询商户详情
     */
    @GetMapping("/{id}")
    public CommonResult<ShopDetailVO> queryById(@PathVariable("id") Long id){
        Long userId = null;
        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            userId = loginUser.getUserId();
        }
        return CommonResult.success(shopService.queryById(id, userId));
    }
    /**
     * 查询附近的商户接口（滚动加载）
     */
    @GetMapping("/nearby")
    public CommonResult<NearbyShopPageVO> queryNearby(
            @RequestParam(value = "typeId", required = false) Long typeId,
            @RequestParam("lng") Double lng,
            @RequestParam("lat") Double lat,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(value = "lastDistance", required = false) Double lastDistance,
            HttpServletRequest request) {
        Long userId = null;
        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            userId = loginUser.getUserId();
        }
        geoRateLimiter.checkReadLimit(userId, ClientIpUtils.resolve(request));
        return CommonResult.success(nearbyShopQueryService.queryNearby(typeId, lng, lat, pageSize, lastDistance));
    }

}
