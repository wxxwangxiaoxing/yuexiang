package com.yuexiang.shop.service;

import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.shop.domain.vo.ShopListVO;

public interface ShopSearchService {

    PageResult<ShopListVO> searchShops(String keyword, Long typeId, String area,
                                       Double longitude, Double latitude,
                                       Integer minPrice, Integer maxPrice,
                                       Integer pageNo, Integer pageSize, Long userId);
}
