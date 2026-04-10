package com.yuexiang.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.shop.domain.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT COUNT(*) FROM tb_review WHERE shop_id = #{shopId} AND status = 1 AND deleted = 0")
    int countPublishedByShopId(@Param("shopId") Long shopId);
}
