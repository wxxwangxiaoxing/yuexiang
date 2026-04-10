package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.dto.CommentCreateDTO;
import com.yuexiang.blog.domain.vo.CommentListVO;
import com.yuexiang.blog.domain.vo.CommentCreateVO;

public interface BlogCommentService {

    CommentListVO getComments(Long blogId, Integer page, Integer size, String sortBy, Long currentUserId);

    CommentCreateVO createComment(Long blogId, CommentCreateDTO dto, Long userId);

    Long getTotalCommentCount(Long blogId);
}
