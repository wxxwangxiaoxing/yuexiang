package com.yuexiang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.blog.domain.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component("blogFavoriteMapper")
public interface BlogFavoriteMapper extends BaseMapper<Favorite> {
}
