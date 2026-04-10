package com.yuexiang.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.shop.domain.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    List<String> selectTagsByShopId(@Param("shopId") Long shopId);
}
