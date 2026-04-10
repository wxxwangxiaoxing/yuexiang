package com.yuexiang.framework.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String messageId;

    private String topic;

    private String tag;

    private T payload;

    private LocalDateTime createTime;

    private int reconsumeTimes;

    public static <T> MqMessage<T> of(String topic, T payload) {
        return MqMessage.<T>builder()
                .topic(topic)
                .payload(payload)
                .createTime(LocalDateTime.now())
                .reconsumeTimes(0)
                .build();
    }

    public static <T> MqMessage<T> of(String topic, String tag, T payload) {
        return MqMessage.<T>builder()
                .topic(topic)
                .tag(tag)
                .payload(payload)
                .createTime(LocalDateTime.now())
                .reconsumeTimes(0)
                .build();
    }
}
