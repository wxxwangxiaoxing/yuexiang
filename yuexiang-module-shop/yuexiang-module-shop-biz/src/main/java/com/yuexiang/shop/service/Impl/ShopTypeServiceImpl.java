package com.yuexiang.shop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.ShopTypeVO;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import com.yuexiang.shop.service.ShopTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopTypeServiceImpl implements ShopTypeService {
    private final ShopTypeMapper shopTypeMapper;
    @Override
    public List<ShopTypeVO> queryOrderBySort() {
        // 1. 查询数据库实体列表
        List<ShopType> typeList = shopTypeMapper.selectList(
                new LambdaQueryWrapper<ShopType>()
                        .orderByAsc(ShopType::getSort)
        );
        // 2. 将 Entity 转换为 VO
        return BeanUtil.copyToList(typeList, ShopTypeVO.class);
    }
}

