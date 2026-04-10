package com.yuexiang.shop.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.ShopDetailVO;
import com.yuexiang.shop.mapper.ReviewMapper;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import com.yuexiang.shop.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShopDetailSupport {

    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final Long CACHE_NULL_TTL = 2L;

    private final StringRedisTemplate redisTemplate;
    private final ShopTypeMapper shopTypeMapper;
    private final ReviewMapper reviewMapper;
    private final FavoriteService favoriteService;
    private final ObjectMapper objectMapper;

    public String getCachedShop(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void cacheValue(String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis SET 异常，key={}", key, e);
        }
    }

    public long buildShopCacheTtl() {
        return CACHE_SHOP_TTL + RandomUtil.randomLong(1, 5);
    }

    public ShopDetailVO buildShopDetailVO(Shop shop) {
        ShopDetailVO shopDetailVO = new ShopDetailVO();
        BeanUtil.copyProperties(shop, shopDetailVO);
        shopDetailVO.setImages(parseImages(shop.getImages()));
        shopDetailVO.setOpenHours(parseOpenHours(shop.getOpenHours()));

        if (shop.getAvgPrice() != null) {
            BigDecimal priceInYuan = NumberUtil.div(new BigDecimal(shop.getAvgPrice()), new BigDecimal(100), 2);
            shopDetailVO.setAvgPrice(priceInYuan.toString());
        }

        shopDetailVO.setScore(shop.getScore() != null ? shop.getScore().doubleValue() : 0.0);
        shopDetailVO.setLongitude(shop.getLongitude() != null ? shop.getLongitude().doubleValue() : null);
        shopDetailVO.setLatitude(shop.getLatitude() != null ? shop.getLatitude().doubleValue() : null);

        ShopType shopType = shopTypeMapper.selectById(shop.getTypeId());
        shopDetailVO.setTypeName(shopType != null ? shopType.getName() : null);
        shopDetailVO.setReviewCount(reviewMapper.countPublishedByShopId(shop.getId()));
        return shopDetailVO;
    }

    public void enrichDynamicFields(ShopDetailVO shopDetailVO, Long userId) {
        shopDetailVO.setIsFavorite(userId != null && favoriteService.isFavorite(userId, shopDetailVO.getId()));
        shopDetailVO.setIsOpen(checkIsOpen(shopDetailVO.getOpenHours()));
    }

    public String toJson(ShopDetailVO shopDetailVO) {
        return JSONUtil.toJsonStr(shopDetailVO);
    }

    private List<String> parseImages(String images) {
        if (StrUtil.isBlank(images)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(images, String.class).stream()
                    .map(url -> url.replace("\"", "").trim())
                    .toList();
        } catch (Exception e) {
            log.error("图片解析失败", e);
            return Collections.emptyList();
        }
    }

    private String parseOpenHours(String openHours) {
        if (StrUtil.isBlank(openHours)) {
            return openHours;
        }
        try {
            return String.valueOf(JSONUtil.parseObj(openHours));
        } catch (Exception e) {
            log.error("营业时间解析失败，原始数据: {}", openHours);
            return openHours;
        }
    }

    private boolean checkIsOpen(String openHoursJson) {
        if (StrUtil.isBlank(openHoursJson)) {
            return true;
        }
        try {
            Map<String, String> openHours = objectMapper.readValue(
                    openHoursJson, new TypeReference<Map<String, String>>() {});
            DayOfWeek today = DayOfWeek.of(LocalDateTime.now().getDayOfWeek().getValue());
            String dayKey = today.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH).toLowerCase();
            String timeRange = openHours.get(dayKey);
            if (timeRange == null || timeRange.isEmpty()) {
                return false;
            }
            String[] parts = timeRange.split("-");
            if (parts.length != 2) {
                return false;
            }
            LocalTime now = LocalTime.now();
            LocalTime open = LocalTime.parse(parts[0].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime close = LocalTime.parse(parts[1].trim(), DateTimeFormatter.ofPattern("HH:mm"));
            if (close.isBefore(open)) {
                return !now.isBefore(open) || !now.isAfter(close);
            }
            return !now.isBefore(open) && !now.isAfter(close);
        } catch (Exception e) {
            log.warn("营业时间解析失败，默认营业中: {}", openHoursJson);
            return true;
        }
    }
}
