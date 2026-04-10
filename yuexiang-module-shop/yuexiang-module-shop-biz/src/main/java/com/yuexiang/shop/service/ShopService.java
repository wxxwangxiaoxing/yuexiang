package com.yuexiang.shop.service;

import com.yuexiang.shop.domain.vo.ShopDetailVO;

public interface ShopService {

    ShopDetailVO queryById(Long id);

    ShopDetailVO queryById(Long id, Long userId);

}
