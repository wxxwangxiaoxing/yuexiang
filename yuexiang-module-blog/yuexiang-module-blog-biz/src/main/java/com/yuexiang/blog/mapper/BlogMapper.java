package com.yuexiang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.blog.domain.entity.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {

    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);

    int updateFavoriteCount(@Param("id") Long id, @Param("delta") int delta);

    Integer getFavoriteCount(@Param("id") Long id);
}
