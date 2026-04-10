package com.yuexiang.shop.service;

import com.yuexiang.shop.domain.vo.ShopAreaVO;

import java.util.List;

public interface ShopAreaService {

    List<ShopAreaVO> queryAreaList(Long typeId, String cityCode);
}
