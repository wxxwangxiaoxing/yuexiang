package com.yuexiang.blog.service;

public interface BlogLikeService {

    void likeBlog(Long userId, Long blogId);

    void unlikeBlog(Long userId, Long blogId);

    boolean isLiked(Long userId, Long blogId);

    Long getBlogLikeCount(Long blogId);
}
