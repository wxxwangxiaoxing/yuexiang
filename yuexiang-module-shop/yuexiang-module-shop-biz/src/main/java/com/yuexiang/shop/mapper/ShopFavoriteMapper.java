package com.yuexiang.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.shop.domain.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ShopFavoriteMapper extends BaseMapper<Favorite> {
//
//    Set<Long> selectFavoriteShopIds(@Param("userId") Long userId,
//                                    @Param("shopIds") List<Long> shopIds);
}
