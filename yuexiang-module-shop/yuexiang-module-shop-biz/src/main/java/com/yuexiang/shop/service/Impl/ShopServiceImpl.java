package com.yuexiang.shop.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.vo.ShopDetailVO;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.service.ShopService;
import com.yuexiang.shop.support.ShopDetailSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ShopServiceImpl implements ShopService {

    // ==================== 缓存常量 ====================
    private final ShopMapper shopMapper;
    private final ShopDetailSupport shopDetailSupport;

    @Autowired
    public ShopServiceImpl(ShopMapper shopMapper, ShopDetailSupport shopDetailSupport) {
        this.shopMapper = shopMapper;
        this.shopDetailSupport = shopDetailSupport;
    }

    @Override
    public ShopDetailVO queryById(Long id) {
        return queryById(id, null);
    }

    public ShopDetailVO queryById(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "店铺 ID 不能为空");
        }

        String key = ShopDetailSupport.CACHE_SHOP_KEY + id;
        String shopJson = shopDetailSupport.getCachedShop(key);

        if (StrUtil.isNotBlank(shopJson)) {
            ShopDetailVO cached = JSONUtil.toBean(shopJson, ShopDetailVO.class);
            shopDetailSupport.enrichDynamicFields(cached, userId);
            return cached;
        }

        if (shopJson != null) {
            throw new BusinessException(ResultCodeEnum.SHOP_NOT_FOUND, "店铺不存在");
        }

        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            shopDetailSupport.cacheValue(key, "", ShopDetailSupport.CACHE_NULL_TTL, TimeUnit.MINUTES);
            throw new BusinessException(ResultCodeEnum.SHOP_NOT_FOUND, "店铺不存在");
        }

        ShopDetailVO shopDetailVO = shopDetailSupport.buildShopDetailVO(shop);
        String jsonStr = shopDetailSupport.toJson(shopDetailVO);
        shopDetailSupport.cacheValue(key, jsonStr, shopDetailSupport.buildShopCacheTtl(), TimeUnit.MINUTES);
        shopDetailSupport.enrichDynamicFields(shopDetailVO, userId);
        return shopDetailVO;
    }
}
