package com.yuexiang.user.service.impl;

import com.yuexiang.user.api.UserLikeService;
import com.yuexiang.user.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLikeServiceImpl implements UserLikeService {

    private final UserInfoMapper userInfoMapper;

    @Override
    public void updateLikeCount(Long userId, Integer delta) {
        if (userId == null || delta == null || delta == 0) {
            return;
        }
        
        try {
            userInfoMapper.updateLikeCount(userId, delta);
            log.info("[更新用户获赞数] userId={}, delta={}", userId, delta);
        } catch (Exception e) {
            log.error("[更新用户获赞数失败] userId={}, delta={}", userId, delta, e);
            throw new RuntimeException("更新用户获赞数失败", e);
        }
    }

    @Override
    public void batchUpdateLikeCount(Map<Long, Integer> userLikeDeltas) {
        if (userLikeDeltas == null || userLikeDeltas.isEmpty()) {
            return;
        }
        
        try {
            for (Map.Entry<Long, Integer> entry : userLikeDeltas.entrySet()) {
                if (entry.getValue() != null && entry.getValue() != 0) {
                    userInfoMapper.updateLikeCount(entry.getKey(), entry.getValue());
                }
            }
            log.info("[批量更新用户获赞数] userCount={}", userLikeDeltas.size());
        } catch (Exception e) {
            log.error("[批量更新用户获赞数失败] userLikeDeltas={}", userLikeDeltas, e);
            throw new RuntimeException("批量更新用户获赞数失败", e);
        }
    }

    @Override
    public List<Long> selectUserIdsByPage(int offset, int limit) {
        try {
            return userInfoMapper.selectUserIdsByPage(offset, limit);
        } catch (Exception e) {
            log.error("[分页查询用户ID失败] offset={}, limit={}", offset, limit, e);
            throw new RuntimeException("分页查询用户ID失败", e);
        }
    }

    @Override
    public Integer selectLikeCount(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            return userInfoMapper.selectLikeCount(userId);
        } catch (Exception e) {
            log.error("[查询用户获赞数失败] userId={}", userId, e);
            throw new RuntimeException("查询用户获赞数失败", e);
        }
    }

    @Override
    public void updateLikeCountDirectly(Long userId, long realCount) {
        if (userId == null) {
            return;
        }
        
        try {
            userInfoMapper.updateLikeCountDirectly(userId, realCount);
            log.info("[直接更新用户获赞数] userId={}, realCount={}", userId, realCount);
        } catch (Exception e) {
            log.error("[直接更新用户获赞数失败] userId={}, realCount={}", userId, realCount, e);
            throw new RuntimeException("直接更新用户获赞数失败", e);
        }
    }
}
