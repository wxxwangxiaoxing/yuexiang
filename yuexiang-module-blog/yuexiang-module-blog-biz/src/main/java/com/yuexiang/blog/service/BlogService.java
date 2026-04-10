package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.vo.BlogListItemVO;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.blog.domain.vo.BlogDetailVO;

public interface BlogService {

    BlogDetailVO getBlogDetail(Long blogId, Long currentUserId);

    PageResult<BlogListItemVO> getBlogList(Long currentUserId, Integer pageNo, Integer pageSize);

    void incrementLikeCount(Long blogId);

    void decrementLikeCount(Long blogId);
}
