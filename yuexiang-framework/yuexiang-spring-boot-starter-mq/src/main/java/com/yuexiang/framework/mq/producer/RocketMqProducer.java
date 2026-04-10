package com.yuexiang.framework.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq.producer", name = "enabled", havingValue = "true")
public class RocketMqProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    public SendResult syncSend(String topic, Object payload) {
        return syncSend(topic, null, payload);
    }

    public SendResult syncSend(String topic, String tag, Object payload) {
        String destination = buildDestination(topic, tag);
        try {
            String json = serializePayload(payload);
            Message<String> message = MessageBuilder.withPayload(json).build();
            SendResult result = rocketMQTemplate.syncSend(destination, message);
            log.debug("[RocketMqProducer] 同步发送成功: destination={}, msgId={}", destination, result.getMsgId());
            return result;
        } catch (Exception e) {
            log.error("[RocketMqProducer] 同步发送失败: destination={}", destination, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    public void asyncSend(String topic, Object payload) {
        asyncSend(topic, null, payload, null);
    }

    public void asyncSend(String topic, Object payload, SendCallback callback) {
        asyncSend(topic, null, payload, callback);
    }

    public void asyncSend(String topic, String tag, Object payload, SendCallback callback) {
        String destination = buildDestination(topic, tag);
        try {
            String json = serializePayload(payload);
            Message<String> message = MessageBuilder.withPayload(json).build();
            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.debug("[RocketMqProducer] 异步发送成功: destination={}, msgId={}", destination, sendResult.getMsgId());
                    if (callback != null) {
                        callback.onSuccess(sendResult);
                    }
                }

                @Override
                public void onException(Throwable e) {
                    log.error("[RocketMqProducer] 异步发送失败: destination={}", destination, e);
                    if (callback != null) {
                        callback.onException(e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("[RocketMqProducer] 异步发送异常: destination={}", destination, e);
        }
    }

    public CompletableFuture<SendResult> asyncSendFuture(String topic, Object payload) {
        return asyncSendFuture(topic, null, payload);
    }

    public CompletableFuture<SendResult> asyncSendFuture(String topic, String tag, Object payload) {
        CompletableFuture<SendResult> future = new CompletableFuture<>();
        asyncSend(topic, tag, payload, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                future.complete(sendResult);
            }

            @Override
            public void onException(Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public void sendOneWay(String topic, Object payload) {
        sendOneWay(topic, null, payload);
    }

    public void sendOneWay(String topic, String tag, Object payload) {
        String destination = buildDestination(topic, tag);
        try {
            String json = serializePayload(payload);
            Message<String> message = MessageBuilder.withPayload(json).build();
            rocketMQTemplate.sendOneWay(destination, message);
            log.debug("[RocketMqProducer] 单向发送成功: destination={}", destination);
        } catch (Exception e) {
            log.error("[RocketMqProducer] 单向发送失败: destination={}", destination, e);
        }
    }

    public SendResult syncSendDelayMessage(String topic, Object payload, int delayLevel) {
        return syncSendDelayMessage(topic, null, payload, delayLevel);
    }

    public SendResult syncSendDelayMessage(String topic, String tag, Object payload, int delayLevel) {
        String destination = buildDestination(topic, tag);
        try {
            String json = serializePayload(payload);
            Message<String> message = MessageBuilder.withPayload(json).build();
            SendResult result = rocketMQTemplate.syncSend(destination, message, 3000, delayLevel);
            log.debug("[RocketMqProducer] 延迟消息发送成功: destination={}, msgId={}, delayLevel={}", 
                    destination, result.getMsgId(), delayLevel);
            return result;
        } catch (Exception e) {
            log.error("[RocketMqProducer] 延迟消息发送失败: destination={}", destination, e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }

    private String buildDestination(String topic, String tag) {
        if (tag != null && !tag.isEmpty()) {
            return topic + ":" + tag;
        }
        return topic;
    }

    private String serializePayload(Object payload) throws JsonProcessingException {
        if (payload == null) {
            return null;
        }
        if (payload instanceof String) {
            return (String) payload;
        }
        return objectMapper.writeValueAsString(payload);
    }
}
