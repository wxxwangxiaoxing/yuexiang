package com.yuexiang.shop.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.shop.constants.ShopListConstants;
import com.yuexiang.shop.domain.dto.ShopListQueryDTO;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.ShopListPageVO;
import com.yuexiang.shop.domain.vo.ShopListVO;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import com.yuexiang.shop.service.ShopListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopListServiceImpl implements ShopListService {

    private final ShopMapper shopMapper;
    private final ShopTypeMapper shopTypeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ShopListPageVO queryShopList(ShopListQueryDTO query, Long userId) {
        validateQuery(query);
        
        int pageSize = Math.min(query.getPageSize(), ShopListConstants.MAX_PAGE_SIZE);
        int offset = (query.getPageNo() - 1) * pageSize;
        
        List<Shop> shops;
        if (ShopListConstants.DISTANCE_SORT_BY.equals(query.getSortBy())) {
            shops = shopMapper.selectShopListWithDistance(
                    query.getTypeId(), query.getKeyword(), query.getArea(),
                    query.getMinPrice(), query.getMaxPrice(),
                    query.getLongitude(), query.getLatitude(),
                    query.getSortBy(), offset, pageSize
            );
        } else {
            shops = shopMapper.selectShopListBasic(
                    query.getTypeId(), query.getKeyword(), query.getArea(),
                    query.getMinPrice(), query.getMaxPrice(),
                    query.getSortBy(), offset, pageSize
            );
        }
        
        Long total = shopMapper.countShopList(
                query.getTypeId(), query.getKeyword(), query.getArea(),
                query.getMinPrice(), query.getMaxPrice()
        );
        
        List<Long> shopIds = shops.stream().map(Shop::getId).collect(Collectors.toList());
        
        Map<Long, List<String>> tagMap = batchLoadTags(shopIds);
        
        Set<Long> favoriteIds = Collections.emptySet();
        if (userId != null && !shopIds.isEmpty()) {
            favoriteIds = shopMapper.selectFavoriteShopIds(userId, shopIds);
        }
        
        Map<Long, String> typeNameMap = loadTypeNames(shops);
        
        Map<Long, String> aiSummaryMap = batchLoadAiSummaries(shopIds);
        
        final Set<Long> finalFavoriteIds = favoriteIds;
        List<ShopListVO> records = shops.stream()
                .map(shop -> toShopListVO(shop, tagMap, finalFavoriteIds, typeNameMap, aiSummaryMap))
                .collect(Collectors.toList());
        
        int pages = (int) Math.ceil((double) total / pageSize);
        
        return ShopListPageVO.builder()
                .total(total)
                .pages(pages)
                .current(query.getPageNo())
                .records(records)
                .build();
    }

    private void validateQuery(ShopListQueryDTO query) {
        if (query.getPageNo() == null || query.getPageNo() < 1) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(ShopListConstants.DEFAULT_PAGE_SIZE);
        }
        if (query.getPageSize() > ShopListConstants.MAX_PAGE_SIZE) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "pageSize 不能超过 " + ShopListConstants.MAX_PAGE_SIZE);
        }
        
        String sortBy = query.getSortBy();
        if (sortBy == null || sortBy.isEmpty()) {
            query.setSortBy(ShopListConstants.DEFAULT_SORT_BY);
        } else if (!ShopListConstants.VALID_SORT_BY.contains(sortBy)) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "无效的排序方式");
        }

        if (ShopListConstants.DISTANCE_SORT_BY.equals(query.getSortBy())) {
            if (query.getLongitude() == null || query.getLatitude() == null) {
                throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "排序方式选择距离时经纬度不能为空");
            }
        }

        if (query.getKeyword() != null && query.getKeyword().length() > ShopListConstants.MAX_KEYWORD_LENGTH) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "搜索关键词长度不能超过" + ShopListConstants.MAX_KEYWORD_LENGTH);
        }
    }

    private Map<Long, List<String>> batchLoadTags(List<Long> shopIds) {
        Map<Long, List<String>> tagMap = new HashMap<>();
        for (Long shopId : shopIds) {
            List<String> tags = shopMapper.selectShopTags(shopId);
            tagMap.put(shopId, tags != null ? tags : Collections.emptyList());
        }
        return tagMap;
    }

    private Map<Long, String> loadTypeNames(List<Shop> shops) {
        Map<Long, String> typeNameMap = new HashMap<>();
        Set<Long> typeIds = shops.stream()
                .map(Shop::getTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        for (Long typeId : typeIds) {
            ShopType shopType = shopTypeMapper.selectById(typeId);
            if (shopType != null) {
                typeNameMap.put(typeId, shopType.getName());
            }
        }
        return typeNameMap;
    }

    private Map<Long, String> batchLoadAiSummaries(List<Long> shopIds) {
        Map<Long, String> summaryMap = new HashMap<>();
        for (Long shopId : shopIds) {
            String summary = shopMapper.selectAiSummary(shopId);
            if (summary != null) {
                summaryMap.put(shopId, summary);
            }
        }
        return summaryMap;
    }

    private ShopListVO toShopListVO(Shop shop, Map<Long, List<String>> tagMap,
                                    Set<Long> favoriteIds, Map<Long, String> typeNameMap,
                                    Map<Long, String> aiSummaryMap) {
        ShopListVO vo = new ShopListVO();
        vo.setId(shop.getId());
        vo.setName(shop.getName());
        vo.setTypeName(typeNameMap.get(shop.getTypeId()));
        vo.setArea(shop.getArea());
        vo.setAddress(shop.getAddress());
        
        if (shop.getLongitude() != null) {
            vo.setLongitude(shop.getLongitude().doubleValue());
        }
        if (shop.getLatitude() != null) {
            vo.setLatitude(shop.getLatitude().doubleValue());
        }
        if (shop.getScore() != null) {
            vo.setScore(shop.getScore().doubleValue());
        }
        if (shop.getAvgPrice() != null) {
            vo.setAvgPrice(shop.getAvgPrice() / 100);
        }
        
        vo.setSalesCount(shop.getSalesCount());
        vo.setCommentCount(shop.getCommentCount());
        
        if (shop.getDistance() != null) {
            vo.setDistance(shop.getDistance().setScale(0, RoundingMode.HALF_UP).intValue());
        }
        
        vo.setImages(parseImages(shop.getImages()));
        vo.setTags(tagMap.getOrDefault(shop.getId(), Collections.emptyList()));
        vo.setAiSummary(aiSummaryMap.get(shop.getId()));
        vo.setIsFavorite(favoriteIds.contains(shop.getId()));
        
        return vo;
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<String> images = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            return images.stream().limit(ShopListConstants.MAX_IMAGE_COUNT).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("解析商户图片JSON失败: {}", imagesJson, e);
            return Collections.emptyList();
        }
    }
}
