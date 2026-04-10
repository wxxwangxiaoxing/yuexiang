package com.yuexiang.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.ai.domain.vo.RecommendedShopVO;
import com.yuexiang.ai.functions.BlogSearchRequest;
import com.yuexiang.ai.functions.BlogSummaryVO;
import com.yuexiang.ai.functions.ShopSearchRequest;
import com.yuexiang.ai.service.ShopRecommendService;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.vo.ShopVO;
import com.yuexiang.shop.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopRecommendServiceImpl implements ShopRecommendService {

    private final ShopMapper shopMapper;

    @Override
    public List<RecommendedShopVO> searchRecommendedShops(ShopSearchRequest request) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getDeleted, 0);

        if (request.getBudgetMax() != null) {
            wrapper.le(Shop::getAvgPrice, request.getBudgetMax());
        }

        if (request.getBudgetMin() != null) {
            wrapper.ge(Shop::getAvgPrice, request.getBudgetMin());
        }

        wrapper.orderByDesc(Shop::getScore);
        wrapper.last("LIMIT 10");

        List<Shop> shops = shopMapper.selectList(wrapper);

        return shops.stream()
                .map(this::convertToRecommendedShopVO)
                .toList();
    }

    @Override
    public ShopVO getShopDetail(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getDeleted() == 1) {
            return null;
        }
        return convertToShopVO(shop);
    }

    @Override
    public List<BlogSummaryVO> searchBlogs(BlogSearchRequest request) {
        return Collections.emptyList();
    }

    private RecommendedShopVO convertToRecommendedShopVO(Shop shop) {
        return RecommendedShopVO.builder()
                .shopId(shop.getId())
                .name(shop.getName())
                .image(shop.getImages())
                .typeName("美食")
                .score(shop.getScore() != null ? shop.getScore() : BigDecimal.ZERO)
                .avgPrice(shop.getAvgPrice())
                .distance(0.0)
                .address(shop.getAddress())
                .reason("评分高，口碑好")
                .build();
    }

    private ShopVO convertToShopVO(Shop shop) {
        ShopVO vo = new ShopVO();
        vo.setId(shop.getId());
        vo.setName(shop.getName());
        vo.setImages(shop.getImages());
        vo.setScore(shop.getScore());
        vo.setAvgPrice(shop.getAvgPrice());
        vo.setAddress(shop.getAddress());
        return vo;
    }
}
