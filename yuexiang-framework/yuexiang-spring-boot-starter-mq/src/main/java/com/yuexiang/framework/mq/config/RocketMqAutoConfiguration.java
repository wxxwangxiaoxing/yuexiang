package com.yuexiang.framework.mq.config; 
 
 import com.fasterxml.jackson.databind.ObjectMapper; 
 import com.yuexiang.framework.mq.producer.RocketMqProducer; 
 import lombok.RequiredArgsConstructor; 
 import lombok.extern.slf4j.Slf4j; 
 import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration; 
 import org.apache.rocketmq.spring.core.RocketMQTemplate; 
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "rocketmq", name = "enabled", havingValue = "true")
@Import(RocketMQAutoConfiguration.class)
@EnableConfigurationProperties({RocketMqProperties.class})
@RequiredArgsConstructor
public class RocketMqAutoConfiguration {
 
     private final RocketMqProperties rocketMqProperties; 
     private final RocketMQTemplate rocketMQTemplate; 
     private final ObjectMapper objectMapper; 
 
     @Bean 
     @ConditionalOnMissingBean 
     @ConditionalOnProperty(prefix = "rocketmq.producer", name = "enabled", havingValue = "true", matchIfMissing = true) 
     public RocketMqProducer rocketMqProducer() { 
         log .info("[RocketMqAutoConfiguration] 初始化RocketMQ Producer, nameServer={}", rocketMqProperties.getNameServer()); 
         return new RocketMqProducer(rocketMQTemplate, objectMapper); 
     } 
 
     @Bean 
     public RocketMqInitializer rocketMqInitializer() { 
         log .info("[RocketMqAutoConfiguration] RocketMQ Starter 初始化完成: {}", rocketMqProperties); 
         return new RocketMqInitializer(); 
     } 
 
     public static class RocketMqInitializer { 
         public RocketMqInitializer() { 
             log .info("[RocketMqInitializer] RocketMQ Starter 已就绪"); 
         } 
     } 
 }
