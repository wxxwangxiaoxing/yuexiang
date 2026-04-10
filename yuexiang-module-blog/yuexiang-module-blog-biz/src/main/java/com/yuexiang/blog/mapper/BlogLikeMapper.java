package com.yuexiang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.blog.domain.entity.BlogLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BlogLikeMapper extends BaseMapper<BlogLike> {

    int deleteByUserAndBlog(@Param("userId") Long userId, @Param("blogId") Long blogId);

    int countByUserAndBlog(@Param("userId") Long userId, @Param("blogId") Long blogId);

    long countByAuthorId(@Param("authorId") Long authorId);
}
