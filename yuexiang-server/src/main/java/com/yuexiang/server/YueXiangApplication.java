package com.yuexiang.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.yuexiang.**.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(scanBasePackages = "com.yuexiang", exclude = {
    org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
public class YueXiangApplication {

    public static void main(String[] args) {
        SpringApplication.run(YueXiangApplication.class, args);
    }
}
