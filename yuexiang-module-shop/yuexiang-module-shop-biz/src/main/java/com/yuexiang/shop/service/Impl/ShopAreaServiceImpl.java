package com.yuexiang.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.shop.constants.ShopAreaConstants;
import com.yuexiang.shop.domain.vo.ShopAreaVO;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.service.ShopAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopAreaServiceImpl implements ShopAreaService {

    private final ShopMapper shopMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public List<ShopAreaVO> queryAreaList(Long typeId, String cityCode) {
        String cacheKey = buildCacheKey(typeId, cityCode);
        
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return parseCachedResult(cached);
        }
        
        List<ShopAreaVO> areas = queryFromDatabase(typeId);
        
        if (!areas.isEmpty()) {
            cacheResult(cacheKey, areas);
        }
        
        return areas;
    }

    private String buildCacheKey(Long typeId, String cityCode) {
        StringBuilder key = new StringBuilder(ShopAreaConstants.AREA_LIST_CACHE_KEY);
        if (typeId != null) {
            key.append(":type:").append(typeId);
        }
        if (cityCode != null) {
            key.append(":city:").append(cityCode);
        }
        return key.toString();
    }

    private List<ShopAreaVO> queryFromDatabase(Long typeId) {
        List<ShopAreaVO> result = new ArrayList<>();
        
        List<Map<String, Object>> areaStats = shopMapper.selectAreaStats(typeId);
        for (Map<String, Object> stat : areaStats) {
            ShopAreaVO vo = new ShopAreaVO();
            vo.setArea(String.valueOf(stat.get("area")));
            Object countObj = stat.get("shop_count");
            if (countObj instanceof Number) {
                vo.setShopCount(((Number) countObj).intValue());
            }
            result.add(vo);
        }
        
        return result;
    }

    private List<ShopAreaVO> parseCachedResult(String cached) {
        try {
            List<ShopAreaVO> result = new ArrayList<>();
            String[] items = cached.split("\\|");
            for (String item : items) {
                String[] parts = item.split(ShopAreaConstants.CACHE_FIELD_SEPARATOR);
                if (parts.length == ShopAreaConstants.CACHE_ITEM_PARTS) {
                    ShopAreaVO vo = new ShopAreaVO();
                    vo.setArea(parts[0]);
                    vo.setShopCount(Integer.parseInt(parts[1]));
                    result.add(vo);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("解析商圈缓存失败: {}", cached, e);
            return new ArrayList<>();
        }
    }

    private void cacheResult(String cacheKey, List<ShopAreaVO> areas) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < areas.size(); i++) {
                if (i > 0) sb.append(ShopAreaConstants.CACHE_ITEM_SEPARATOR);
                sb.append(areas.get(i).getArea())
                        .append(ShopAreaConstants.CACHE_FIELD_SEPARATOR)
                        .append(areas.get(i).getShopCount());
            }
            redisTemplate.opsForValue().set(cacheKey, sb.toString(), ShopAreaConstants.CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存商圈列表失败: cacheKey={}", cacheKey, e);
        }
    }
}
