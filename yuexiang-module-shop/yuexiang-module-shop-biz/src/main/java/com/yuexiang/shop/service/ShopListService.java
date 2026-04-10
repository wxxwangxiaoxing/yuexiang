package com.yuexiang.shop.service;

import com.yuexiang.shop.domain.dto.ShopListQueryDTO;
import com.yuexiang.shop.domain.vo.ShopListPageVO;

public interface ShopListService {

    ShopListPageVO queryShopList(ShopListQueryDTO query, Long userId);
}
