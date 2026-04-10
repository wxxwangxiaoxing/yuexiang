package com.yuexiang.shop.service;

import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.ShopTypeVO;

import java.util.List;

public interface ShopTypeService {
    List<ShopTypeVO> queryOrderBySort();

}
