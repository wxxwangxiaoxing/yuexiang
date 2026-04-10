package com.yuexiang.blog.mq.consumer;

import com.yuexiang.blog.constants.LikeMQConstants;
import com.yuexiang.blog.domain.event.LikeEvent;
import com.yuexiang.user.api.UserLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "rocketmq.consumer", name = "enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = LikeMQConstants.LIKE_EVENT_TOPIC,
        consumerGroup = LikeMQConstants.CG_LIKE_USER_COUNT,
        selectorExpression = "LIKE || UNLIKE",
        maxReconsumeTimes = 16
)
@RequiredArgsConstructor
public class LikeEventConsumer implements RocketMQListener<LikeEvent> {

    private final UserLikeService userLikeService;

    private final ConcurrentHashMap<Long, AtomicInteger> deltaBuffer = new ConcurrentHashMap<>();

    @Override
    public void onMessage(LikeEvent event) {
        try {
            deltaBuffer.computeIfAbsent(event.getAuthorId(), k -> new AtomicInteger(0))
                    .addAndGet(event.getDelta());

            log.debug("[LikeConsumer] 消息聚合. authorId={}, delta={}, bufferSize={}",
                    event.getAuthorId(), event.getDelta(), deltaBuffer.size());

        } catch (Exception e) {
            log.error("[LikeConsumer] 消息处理异常. event={}", event, e);
            throw new RuntimeException("消费失败，等待重试", e);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void flushDeltaBuffer() {
        if (deltaBuffer.isEmpty()) {
            return;
        }

        Map<Long, Integer> snapshot = new HashMap<>();
        deltaBuffer.forEach((userId, delta) -> {
            int value = delta.getAndSet(0);
            if (value != 0) {
                snapshot.put(userId, value);
            }
        });
        deltaBuffer.entrySet().removeIf(e -> e.getValue().get() == 0);

        if (snapshot.isEmpty()) {
            return;
        }

        try {
            userLikeService.batchUpdateLikeCount(snapshot);
            log.info("[LikeConsumer] 批量更新用户获赞数. userCount={}, details={}",
                    snapshot.size(), snapshot);

        } catch (Exception e) {
            log.error("[LikeConsumer] 批量更新失败，数据写回缓冲区. snapshot={}", snapshot, e);
            snapshot.forEach((userId, delta) ->
                    deltaBuffer.computeIfAbsent(userId, k -> new AtomicInteger(0))
                            .addAndGet(delta));
        }
    }
}
