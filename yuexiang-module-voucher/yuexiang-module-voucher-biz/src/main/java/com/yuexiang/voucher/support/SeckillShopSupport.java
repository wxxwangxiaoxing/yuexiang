package com.yuexiang.voucher.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.voucher.domain.dto.SeckillVoucherWithInfo;
import com.yuexiang.voucher.domain.vo.SeckillVoucherDetailVO;
import com.yuexiang.voucher.domain.vo.SeckillVoucherListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeckillShopSupport {

    private final ShopMapper shopMapper;
    private final ObjectMapper objectMapper;

    public Map<Long, Shop> loadShopMap(List<SeckillVoucherWithInfo> voucherInfos) {
        Set<Long> shopIds = voucherInfos.stream()
                .map(SeckillVoucherWithInfo::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (shopIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Shop> shopMap = new HashMap<>();
        for (Shop shop : shopMapper.selectBatchIds(shopIds)) {
            shopMap.put(shop.getId(), shop);
        }
        return shopMap;
    }

    public Shop getShopById(Long shopId) {
        return shopId == null ? null : shopMapper.selectById(shopId);
    }

    public void fillShopInfo(SeckillVoucherListVO.VoucherVO vo, Shop shop) {
        if (shop == null) {
            return;
        }
        vo.setShopName(shop.getName());
        vo.setShopImage(resolveShopImage(shop));
    }

    public void fillShopInfo(SeckillVoucherDetailVO vo, Shop shop) {
        if (shop == null) {
            return;
        }
        vo.setShopName(shop.getName());
        vo.setShopAddress(shop.getAddress());
        vo.setShopImage(resolveShopImage(shop));
    }

    private String resolveShopImage(Shop shop) {
        String images = shop.getImages();
        if (images == null || images.isEmpty()) {
            return null;
        }
        if (!images.startsWith("[")) {
            return images;
        }
        try {
            List<String> imageList = objectMapper.readValue(images, List.class);
            return imageList.isEmpty() ? null : imageList.get(0);
        } catch (JsonProcessingException e) {
            return images;
        }
    }
}
