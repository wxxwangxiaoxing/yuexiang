package com.yuexiang.shop.service;

import com.yuexiang.shop.domain.vo.NearbyShopPageVO;

public interface NearbyShopQueryService {
    
    NearbyShopPageVO queryNearby(Long typeId, Double lng, Double lat, Integer pageSize, Double lastDistance);
}
