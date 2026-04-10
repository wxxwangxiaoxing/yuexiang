package com.yuexiang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.blog.domain.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}
