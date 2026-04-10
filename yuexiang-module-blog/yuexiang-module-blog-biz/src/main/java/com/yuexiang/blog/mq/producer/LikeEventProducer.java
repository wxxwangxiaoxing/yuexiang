package com.yuexiang.blog.mq.producer;

import com.yuexiang.blog.constants.LikeMQConstants;
import com.yuexiang.blog.domain.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq.producer", name = "enabled", havingValue = "true")
public class LikeEventProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void sendLikeEvent(LikeEvent event) {
        String tag = event.getType();
        String destination = LikeMQConstants.LIKE_EVENT_TOPIC + ":" + tag;
        String key = event.getBlogId() + ":" + event.getLikeUserId();

        rocketMQTemplate.asyncSend(destination, 
                MessageBuilder.withPayload(event)
                        .setHeader("KEYS", key)
                        .build(), 
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.debug("[LikeProducer] 消息发送成功. msgId={}, authorId={}, delta={}",
                                sendResult.getMsgId(), event.getAuthorId(), event.getDelta());
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("[LikeProducer] 消息发送失败，校对任务将兜底修复. " +
                                        "authorId={}, blogId={}, delta={}",
                                event.getAuthorId(), event.getBlogId(), event.getDelta(), throwable);
                    }
                });
    }
}
