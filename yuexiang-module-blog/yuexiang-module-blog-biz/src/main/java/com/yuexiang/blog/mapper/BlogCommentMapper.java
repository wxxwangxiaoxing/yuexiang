package com.yuexiang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.blog.domain.entity.BlogComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {

    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);
}
