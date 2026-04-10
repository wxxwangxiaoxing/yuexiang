package com.yuexiang.shop.api;

import com.yuexiang.shop.domain.vo.NearbyShopPageVO;
import com.yuexiang.shop.domain.vo.ShopDetailVO;
import com.yuexiang.shop.domain.vo.ShopRecommendVO;

import java.util.List;

public interface ShopReadService {

    ShopDetailVO getShopDetail(Long shopId, Long userId);

    NearbyShopPageVO queryNearby(Long typeId, Double lng, Double lat, Integer pageSize, Double lastDistance);

    List<ShopRecommendVO> listRecommendShopsByIds(List<Long> shopIds);
}
