package com.yuexiang.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.shop.constants.ShopNearbyConstants;
import com.yuexiang.shop.constants.ShopConstants;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.vo.NearbyShopPageVO;
import com.yuexiang.shop.domain.vo.NearbyShopVO;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.service.NearbyShopQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NearbyShopQueryServiceImpl implements NearbyShopQueryService {

    private final ShopMapper shopMapper;
    private final StringRedisTemplate redisTemplate;

    // ======================== 入口 ========================

    @Override
    public NearbyShopPageVO queryNearby(Long typeId, Double lng, Double lat,
                                        Integer pageSize, Double lastDistance) {
        validateCoordinates(lng, lat);
        int safePageSize = normalizePageSize(pageSize);

        // 有 typeId → 优先走 Redis GEO，失败降级 DB
        if (typeId != null && typeId > 0) {
            return queryByTypeWithGeoFallback(typeId, lng, lat, safePageSize, lastDistance);
        }
        // 无 typeId → 直接走 DB 全类型查询
        return queryFromDb(null, lng, lat, safePageSize, lastDistance);
    }

    // ======================== Redis GEO 查询 ========================

    private NearbyShopPageVO queryByTypeWithGeoFallback(Long typeId, Double lng, Double lat,
                                                        int pageSize, Double lastDistance) {
        String geoKey = ShopConstants.SHOP_GEO_KEY + typeId;
        try {
            return queryFromGeo(geoKey, lng, lat, pageSize, lastDistance);
        } catch (Exception ex) {
            log.warn("Redis GEO 查询失败(typeId={}), 降级到 DB: {}", typeId, ex.getMessage());
            return queryFromDb(typeId, lng, lat, pageSize, lastDistance);
        }
    }

    private NearbyShopPageVO queryFromGeo(String geoKey, Double lng, Double lat,
                                           int pageSize, Double lastDistance) {
        boolean isFirstPage = (lastDistance == null || lastDistance <= 0);
        // 首页: 取 pageSize+1 判断 hasMore；翻页: 多取以跳过 cursor 前数据
        int fetchCount = isFirstPage ? pageSize + 1 : pageSize * ShopNearbyConstants.GEO_FETCH_MULTIPLIER;

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().search(
                geoKey,
                GeoReference.fromCoordinate(lng, lat),
                new Distance(ShopNearbyConstants.MAX_RADIUS_KM, Metrics.KILOMETERS),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                        .includeDistance().sortAscending().limit(fetchCount)
        );

        if (results == null || results.getContent().isEmpty()) {
            return emptyPage();
        }

        // 游标过滤：跳过距离 <= lastDistance 的记录
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> filtered =
                skipBeforeCursor(results.getContent(), lastDistance);

        // 分页截取
        boolean hasMore = filtered.size() > pageSize;
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> pageData =
                hasMore ? filtered.subList(0, pageSize) : filtered;

        // 批量加载店铺详情，保持 GEO 排序
        List<Shop> shops = loadShopsByGeoResults(geoKey, pageData);

        return buildPageVO(
                shops.stream().map(this::toNearbyShopVO).collect(Collectors.toList()),
                hasMore,
                extractLastDistanceMeters(pageData)
        );
    }

    /** 跳过距离 ≤ lastDistance 的记录（cursor 语义：返回严格大于上次最后距离的数据） */
    private List<GeoResult<RedisGeoCommands.GeoLocation<String>>> skipBeforeCursor(
            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content,
            Double lastDistanceMeters) {
        if (lastDistanceMeters == null || lastDistanceMeters <= 0) {
            return content;
        }
        double threshold = lastDistanceMeters + ShopNearbyConstants.CURSOR_TOLERANCE_METERS;
        return content.stream()
                .filter(item -> toMeters(item.getDistance()) > threshold)
                .collect(Collectors.toList());
    }

    private Double extractLastDistanceMeters(
            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> pageData) {
        if (pageData.isEmpty()) return null;
        return toMeters(pageData.get(pageData.size() - 1).getDistance());
    }

    /** Redis GEO 返回的 Distance 转为米 */
    private double toMeters(Distance distance) {
        return distance.getValue() * 1000;
    }

    // ======================== DB 查询（统一处理有/无 typeId） ========================

    private NearbyShopPageVO queryFromDb(Long typeId, Double lng, Double lat,
                                          int pageSize, Double lastDistance) {
        List<Shop> candidates = loadCandidatesFromDb(typeId, lng, lat);
        if (candidates.isEmpty()) {
            return emptyPage();
        }

        // 计算精确距离 → 过滤范围外 → 按距离排序
        List<Shop> nearby = candidates.stream()
                .filter(s -> s.getLongitude() != null && s.getLatitude() != null)
                .peek(s -> s.setDistance(BigDecimal.valueOf(
                        haversineMeters(lat, lng,
                                s.getLatitude().doubleValue(),
                                s.getLongitude().doubleValue()))))
                .filter(s -> s.getDistance().doubleValue() <= ShopNearbyConstants.MAX_RADIUS_METERS)
                .sorted(Comparator.comparingDouble(s -> s.getDistance().doubleValue()))
                .collect(Collectors.toList());

        // 游标过滤
        List<Shop> filtered = skipBeforeCursorDb(nearby, lastDistance);

        // 分页截取
        boolean hasMore = filtered.size() > pageSize;
        List<Shop> pageData = hasMore ? filtered.subList(0, pageSize) : filtered;

        Double newLastDistance = pageData.isEmpty() ? null
                : pageData.get(pageData.size() - 1).getDistance().doubleValue();

        return buildPageVO(
                pageData.stream().map(this::toNearbyShopVO).collect(Collectors.toList()),
                hasMore,
                newLastDistance
        );
    }

    /** 矩形粗筛：利用经纬度范围缩小 DB 查询量 */
    private List<Shop> loadCandidatesFromDb(Long typeId, Double lng, Double lat) {
        double latDelta = ShopNearbyConstants.MAX_RADIUS_METERS / ShopNearbyConstants.METERS_PER_DEGREE_LAT;
        double lngDelta = ShopNearbyConstants.MAX_RADIUS_METERS
                / (ShopNearbyConstants.METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(lat)));

        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .eq(Shop::getDeleted, 0)
                .between(Shop::getLongitude, lng - lngDelta, lng + lngDelta)
                .between(Shop::getLatitude, lat - latDelta, lat + latDelta);

        if (typeId != null) {
            wrapper.eq(Shop::getTypeId, typeId);
        }

        List<Shop> result = shopMapper.selectList(wrapper);
        return result != null ? result : Collections.emptyList();
    }

    private List<Shop> skipBeforeCursorDb(List<Shop> shops, Double lastDistanceMeters) {
        if (lastDistanceMeters == null || lastDistanceMeters <= 0) {
            return shops;
        }
        double threshold = lastDistanceMeters + ShopNearbyConstants.CURSOR_TOLERANCE_METERS;
        return shops.stream()
                .filter(s -> s.getDistance().doubleValue() > threshold)
                .collect(Collectors.toList());
    }

    // ======================== 店铺加载 & 过期清理 ========================

    private List<Shop> loadShopsByGeoResults(
            String geoKey,
            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoResults) {
        if (geoResults.isEmpty()) return Collections.emptyList();

        // 提取有序 ID 列表 + 距离映射
        List<Long> orderedIds = new ArrayList<>(geoResults.size());
        Map<Long, Integer> distanceMap = new HashMap<>(geoResults.size());

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : geoResults) {
            String idText = r.getContent().getName();
            if (!StringUtils.hasText(idText)) continue;
            try {
                Long id = Long.valueOf(idText);
                orderedIds.add(id);
                distanceMap.put(id, (int) Math.round(toMeters(r.getDistance())));
            } catch (NumberFormatException e) {
                log.warn("GEO 中存在非法 shopId: {}", idText);
            }
        }

        // 分批查库
        Map<Long, Shop> shopMap = batchLoadActiveShops(orderedIds);

        // 按 GEO 顺序组装结果，收集过期成员
        List<Shop> result = new ArrayList<>(orderedIds.size());
        List<String> staleMembers = new ArrayList<>();

        for (Long id : orderedIds) {
            Shop shop = shopMap.get(id);
            if (shop == null) {
                staleMembers.add(String.valueOf(id));
                continue;
            }
            shop.setDistance(BigDecimal.valueOf(distanceMap.getOrDefault(id, 0)));
            result.add(shop);
        }

        // 精确清理当前 key，避免 KEYS * 全扫描
        if (!staleMembers.isEmpty()) {
            cleanStaleGeoMembers(geoKey, staleMembers);
        }

        return result;
    }

    private Map<Long, Shop> batchLoadActiveShops(List<Long> ids) {
        Map<Long, Shop> map = new HashMap<>(ids.size());
        for (int i = 0; i < ids.size(); i += ShopNearbyConstants.DB_BATCH_SIZE) {
            List<Long> batch = ids.subList(i, Math.min(i + ShopNearbyConstants.DB_BATCH_SIZE, ids.size()));
            shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                            .in(Shop::getId, batch)
                            .eq(Shop::getDeleted, 0))
                    .forEach(s -> map.put(s.getId(), s));
        }
        return map;
    }

    /** 精确清理指定 GEO key 中的过期成员（不再使用 KEYS 全扫描） */
    private void cleanStaleGeoMembers(String geoKey, List<String> members) {
        try {
            redisTemplate.opsForZSet().remove(geoKey, members.toArray());
            log.info("已清理 GEO 过期成员: key={}, count={}", geoKey, members.size());
        } catch (Exception ex) {
            log.warn("清理 GEO 过期成员失败(key={}): {}", geoKey, ex.getMessage());
        }
    }

    // ======================== VO 构建 & 工具方法 ========================

    private NearbyShopPageVO buildPageVO(List<NearbyShopVO> records, boolean hasMore,
                                          Double lastDistance) {
        NearbyShopPageVO vo = new NearbyShopPageVO();
        vo.setRecords(records);
        vo.setHasMore(hasMore);
        vo.setLastDistance(lastDistance);
        return vo;
    }

    private NearbyShopPageVO emptyPage() {
        return buildPageVO(Collections.emptyList(), false, null);
    }

    private NearbyShopVO toNearbyShopVO(Shop shop) {
        NearbyShopVO vo = new NearbyShopVO();
        vo.setId(shop.getId());
        vo.setName(shop.getName());
        vo.setTypeId(shop.getTypeId());
        vo.setImages(shop.getImages());
        vo.setArea(shop.getArea());
        vo.setAddress(shop.getAddress());
        vo.setLng(shop.getLongitude());
        vo.setLat(shop.getLatitude());
        vo.setAvgPrice(shop.getAvgPrice());
        vo.setScore(shop.getScore());
        vo.setOpenHours(shop.getOpenHours());
        vo.setDistance(shop.getDistance() != null
                ? shop.getDistance().setScale(0, RoundingMode.HALF_UP).intValue()
                : null);
        return vo;
    }

    private void validateCoordinates(Double lng, Double lat) {
        if (lng == null || lng < ShopNearbyConstants.MIN_CHINA_LNG || lng > ShopNearbyConstants.MAX_CHINA_LNG) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "经度超出范围");
        }
        if (lat == null || lat < ShopNearbyConstants.MIN_CHINA_LAT || lat > ShopNearbyConstants.MAX_CHINA_LAT) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "纬度超出范围");
        }
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return ShopNearbyConstants.DEFAULT_PAGE_SIZE;
        return Math.min(pageSize, ShopNearbyConstants.MAX_PAGE_SIZE);
    }

    /** Haversine 公式计算两点间地球表面距离（米） */
    private double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return ShopNearbyConstants.EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
