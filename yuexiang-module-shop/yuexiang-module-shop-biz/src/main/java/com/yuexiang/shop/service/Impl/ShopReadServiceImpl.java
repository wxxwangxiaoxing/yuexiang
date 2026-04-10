package com.yuexiang.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.shop.api.ShopReadService;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.NearbyShopPageVO;
import com.yuexiang.shop.domain.vo.ShopDetailVO;
import com.yuexiang.shop.domain.vo.ShopRecommendVO;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import com.yuexiang.shop.service.NearbyShopQueryService;
import com.yuexiang.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopReadServiceImpl implements ShopReadService {

    private final ShopService shopService;
    private final NearbyShopQueryService nearbyShopQueryService;
    private final ShopMapper shopMapper;
    private final ShopTypeMapper shopTypeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ShopDetailVO getShopDetail(Long shopId, Long userId) {
        return shopService.queryById(shopId, userId);
    }

    @Override
    public NearbyShopPageVO queryNearby(Long typeId, Double lng, Double lat, Integer pageSize, Double lastDistance) {
        return nearbyShopQueryService.queryNearby(typeId, lng, lat, pageSize, lastDistance);
    }

    @Override
    public List<ShopRecommendVO> listRecommendShopsByIds(List<Long> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Shop> shops = shopMapper.selectBatchIds(shopIds);
        if (shops == null || shops.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Shop> shopMap = shops.stream().collect(Collectors.toMap(Shop::getId, item -> item));
        Set<Long> typeIds = shops.stream().map(Shop::getTypeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> typeNameMap = Collections.emptyMap();
        if (!typeIds.isEmpty()) {
            typeNameMap = shopTypeMapper.selectBatchIds(typeIds).stream()
                    .collect(Collectors.toMap(ShopType::getId, ShopType::getName));
        }

        Map<Long, String> finalTypeNameMap = typeNameMap;
        return shopIds.stream()
                .map(shopMap::get)
                .filter(Objects::nonNull)
                .map(shop -> ShopRecommendVO.builder()
                        .shopId(shop.getId())
                        .name(shop.getName())
                        .image(firstImage(shop.getImages()))
                        .typeName(finalTypeNameMap.get(shop.getTypeId()))
                        .score(shop.getScore())
                        .avgPrice(shop.getAvgPrice() != null ? shop.getAvgPrice() / 100 : null)
                        .distance(shop.getDistance() != null ? shop.getDistance().doubleValue() : null)
                        .address(shop.getAddress())
                        .build())
                .toList();
    }

    private String firstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return "";
        }
        try {
            List<String> images = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            return images.isEmpty() ? "" : images.get(0);
        } catch (Exception ex) {
            log.warn("解析商户图片失败: {}", ex.getMessage());
            return imagesJson;
        }
    }
}
