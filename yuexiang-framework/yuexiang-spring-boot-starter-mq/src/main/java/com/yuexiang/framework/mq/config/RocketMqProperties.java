package com.yuexiang.framework.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqProperties {

    private String nameServer = "10.0.20.8:9876";

    private Producer producer = new Producer();

    private Consumer consumer = new Consumer();

    @Data
    public static class Producer {
        private String group = "yuexiang-producer-group";
        private int sendTimeout = 3000;
        private int retryTimes = 2;
        private boolean enabled = true;
    }

    @Data
    public static class Consumer {
        private String group = "yuexiang-consumer-group";
        private int consumeThreadMin = 5;
        private int consumeThreadMax = 20;
        private int maxReconsumeTimes = 16;
        private boolean enabled = true;
    }
}
