package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuexiang.blog.domain.entity.Follow;
import com.yuexiang.blog.domain.vo.FollowVO;
import com.yuexiang.blog.mapper.FollowMapper;
import com.yuexiang.blog.service.FollowService;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FollowVO toggleFollow(Long targetUserId, Long currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            throw new BadRequestException("不能关注自己");
        }

        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, targetUserId);
        Follow follow = followMapper.selectOne(queryWrapper);

        boolean isFollowed;
        if (follow == null) {
            Follow newFollow = new Follow();
            newFollow.setUserId(currentUserId);
            newFollow.setFollowUserId(targetUserId);
            newFollow.setCreateTime(LocalDateTime.now());
            newFollow.setUpdateTime(LocalDateTime.now());
            newFollow.setDeleted(0);
            followMapper.insert(newFollow);
            isFollowed = true;
        } else if (follow.getDeleted() == 1) {
            LambdaUpdateWrapper<Follow> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Follow::getId, follow.getId())
                    .set(Follow::getDeleted, 0)
                    .set(Follow::getUpdateTime, LocalDateTime.now());
            followMapper.update(null, updateWrapper);
            isFollowed = true;
        } else {
            LambdaUpdateWrapper<Follow> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Follow::getId, follow.getId())
                    .set(Follow::getDeleted, 1)
                    .set(Follow::getUpdateTime, LocalDateTime.now());
            followMapper.update(null, updateWrapper);
            isFollowed = false;
        }

        FollowVO vo = new FollowVO();
        vo.setIsFollowed(isFollowed);
        return vo;
    }

    @Override
    public boolean isFollowed(Long targetUserId, Long currentUserId) {
        if (currentUserId == null || targetUserId.equals(currentUserId)) {
            return false;
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, targetUserId)
                .eq(Follow::getDeleted, 0);
        return followMapper.selectCount(wrapper) > 0;
    }
}
