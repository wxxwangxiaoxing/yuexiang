package com.yuexiang.user.api;

import java.util.List;
import java.util.Map;

public interface UserLikeService {
    
    void updateLikeCount(Long userId, Integer delta);
    
    void batchUpdateLikeCount(Map<Long, Integer> userLikeDeltas);
    
    List<Long> selectUserIdsByPage(int offset, int limit);
    
    Integer selectLikeCount(Long userId);
    
    void updateLikeCountDirectly(Long userId, long realCount);
}
