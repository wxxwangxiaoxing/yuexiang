package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.vo.CommentLikeVO;
import com.yuexiang.blog.domain.vo.ReplyListVO;

public interface CommentInteractionService {

    CommentLikeVO toggleCommentLike(Long commentId, Long userId);

    ReplyListVO getReplies(Long commentId, Integer page, Integer size, Long currentUserId);
}
