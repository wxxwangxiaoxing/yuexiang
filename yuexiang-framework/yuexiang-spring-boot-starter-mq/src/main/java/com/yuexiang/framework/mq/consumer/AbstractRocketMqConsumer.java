package com.yuexiang.framework.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQListener;

@Slf4j
public abstract class AbstractRocketMqConsumer<T> implements RocketMQListener<String> {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract String getTopic();

    protected abstract Class<T> getMessageType();

    protected abstract void handleMessage(T message);

    @Override
    public void onMessage(String message) {
        log.debug("[RocketMqConsumer] 收到消息: topic={}, message={}", getTopic(), message);
        try {
            T payload = deserialize(message);
            handleMessage(payload);
            log.debug("[RocketMqConsumer] 消息处理成功: topic={}", getTopic());
        } catch (Exception e) {
            log.error("[RocketMqConsumer] 消息处理失败: topic={}, message={}", getTopic(), message, e);
            throw new RuntimeException("消息处理失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected T deserialize(String message) {
        try {
            Class<T> messageType = getMessageType();
            if (messageType == String.class) {
                return (T) message;
            }
            return objectMapper.readValue(message, messageType);
        } catch (Exception e) {
            log.error("[RocketMqConsumer] 消息反序列化失败: message={}", message, e);
            throw new RuntimeException("消息反序列化失败", e);
        }
    }
}
