package com.yuexiang.blog.service;

import com.yuexiang.blog.domain.vo.FollowVO;

public interface FollowService {

    FollowVO toggleFollow(Long targetUserId, Long currentUserId);

    boolean isFollowed(Long targetUserId, Long currentUserId);
}
