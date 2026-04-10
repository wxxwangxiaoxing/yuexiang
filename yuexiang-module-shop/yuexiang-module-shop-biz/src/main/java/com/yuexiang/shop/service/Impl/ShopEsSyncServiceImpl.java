package com.yuexiang.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.shop.domain.document.ShopDocument;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import com.yuexiang.shop.service.ShopEsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "yuexiang.elasticsearch.enabled", havingValue = "true")
public class ShopEsSyncServiceImpl implements ShopEsSyncService {

    private final ShopMapper shopMapper;
    private final ShopTypeMapper shopTypeMapper;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void syncShopToEs(Long shopId) {
        try {
            Shop shop = shopMapper.selectById(shopId);
            if (shop == null) {
                removeShopFromEs(shopId);
                return;
            }
            ShopDocument doc = convertToDocument(shop);
            elasticsearchOperations.save(doc);
            log.info("同步商户到 ES 成功: shopId={}", shopId);
        } catch (Exception e) {
            log.error("同步商户到 ES 失败: shopId={}", shopId, e);
        }
    }

    @Override
    public void removeShopFromEs(Long shopId) {
        try {
            elasticsearchOperations.delete(String.valueOf(shopId), ShopDocument.class);
            log.info("从 ES 删除商户成功: shopId={}", shopId);
        } catch (Exception e) {
            log.error("从 ES 删除商户失败: shopId={}", shopId, e);
        }
    }

    @Override
    public void syncAllShopsToEs() {
        List<Shop> shops = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>().eq(Shop::getDeleted, 0)
        );
        List<ShopDocument> docs = shops.stream()
                .map(this::convertToDocument)
                .toList();
        docs.forEach(elasticsearchOperations::save);
        log.info("全量同步商户到 ES 成功: count={}", docs.size());
    }

    private ShopDocument convertToDocument(Shop shop) {
        ShopDocument doc = new ShopDocument();
        doc.setId(shop.getId());
        doc.setName(shop.getName());
        doc.setAddress(shop.getAddress());
        doc.setArea(shop.getArea());
        doc.setScore(shop.getScore() != null ? shop.getScore().doubleValue() : 0.0);
        doc.setSalesCount(shop.getSalesCount());
        doc.setAvgPrice(shop.getAvgPrice());
        doc.setOpenHours(shop.getOpenHours());
        doc.setDeleted(shop.getDeleted());

        if (shop.getLongitude() != null && shop.getLatitude() != null) {
            doc.setLocation(shop.getLatitude().doubleValue() + "," + shop.getLongitude().doubleValue());
        }

        if (shop.getTypeId() != null) {
            ShopType type = shopTypeMapper.selectById(shop.getTypeId());
            doc.setTypeName(type != null ? type.getName() : null);
        }

        return doc;
    }
}
