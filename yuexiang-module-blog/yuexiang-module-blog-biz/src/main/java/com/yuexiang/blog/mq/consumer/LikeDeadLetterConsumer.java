package com.yuexiang.blog.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.blog.constants.LikeMQConstants;
import com.yuexiang.blog.domain.entity.LikeDeadLetter;
import com.yuexiang.blog.domain.event.LikeEvent;
import com.yuexiang.blog.mapper.LikeDeadLetterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "rocketmq.consumer", name = "enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = LikeMQConstants.DLQ_TOPIC,
        consumerGroup = LikeMQConstants.CG_LIKE_DEAD_LETTER
)
@RequiredArgsConstructor
public class LikeDeadLetterConsumer implements RocketMQListener<MessageExt> {

    private final LikeDeadLetterMapper deadLetterMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        try {
            String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
            LikeEvent event = objectMapper.readValue(body, LikeEvent.class);

            LikeDeadLetter deadLetter = new LikeDeadLetter();
            deadLetter.setMessageId(messageExt.getMsgId());
            deadLetter.setAuthorId(event.getAuthorId());
            deadLetter.setBlogId(event.getBlogId());
            deadLetter.setLikeUserId(event.getLikeUserId());
            deadLetter.setDelta(event.getDelta());
            deadLetter.setRetryCount(messageExt.getReconsumeTimes());
            deadLetter.setErrorMsg("超过最大重试次数，进入死信队列");
            deadLetter.setStatus(0);
            deadLetter.setCreateTime(LocalDateTime.now());
            deadLetter.setUpdateTime(LocalDateTime.now());
            deadLetterMapper.insert(deadLetter);

            log.warn("[DeadLetter] 死信消息已持久化. msgId={}, authorId={}, blogId={}, delta={}",
                    messageExt.getMsgId(), event.getAuthorId(), event.getBlogId(), event.getDelta());

        } catch (Exception e) {
            log.error("[DeadLetter] 死信消息处理失败. msgId={}", messageExt.getMsgId(), e);
        }
    }
}
