package com.yuexiang.blog.task;

import com.yuexiang.blog.mapper.BlogLikeMapper;
import com.yuexiang.user.api.UserLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeCountReconcileTask {

    private final UserLikeService userLikeService;
    private final BlogLikeMapper blogLikeMapper;

    private static final int BATCH_SIZE = 500;

    @Scheduled(cron = "0 0 3 * * ?")
    public void reconcile() {
        log.info("[Reconcile] 开始每日获赞数校对");
        long startTime = System.currentTimeMillis();
        int totalUsers = 0;
        int fixedUsers = 0;

        int page = 0;
        while (true) {
            List<Long> userIds = userLikeService.selectUserIdsByPage(page * BATCH_SIZE, BATCH_SIZE);
            if (userIds.isEmpty()) {
                break;
            }

            for (Long userId : userIds) {
                totalUsers++;
                try {
                    long realCount = blogLikeMapper.countByAuthorId(userId);
                    Integer currentCount = userLikeService.selectLikeCount(userId);

                    if (currentCount == null || currentCount != realCount) {
                        userLikeService.updateLikeCountDirectly(userId, realCount);
                        fixedUsers++;
                        log.info("[Reconcile] 修正用户获赞数: userId={}, {} → {}",
                                userId, currentCount, realCount);
                    }
                } catch (Exception e) {
                    log.error("[Reconcile] 校对失败: userId={}", userId, e);
                }
            }

            page++;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[Reconcile] 校对完成. 总用户={}, 修正={}, 耗时={}ms",
                totalUsers, fixedUsers, elapsed);
    }
}
