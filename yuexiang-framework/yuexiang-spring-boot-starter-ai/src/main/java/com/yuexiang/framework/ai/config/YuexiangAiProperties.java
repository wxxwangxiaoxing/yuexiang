package com.yuexiang.framework.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yuexiang.ai")
public class YuexiangAiProperties {

    private boolean enabled = false;

    private Provider primary = Provider.DEEPSEEK;

    private DeepSeekConfig deepseek = new DeepSeekConfig();

    private OpenAiConfig openai = new OpenAiConfig();

    private QwenConfig qwen = new QwenConfig();

    private Fallback fallback = new Fallback();

    @Data
    public static class DeepSeekConfig {
        private String apiKey;
        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
    }

    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String baseUrl = "https://api.openai.com";
        private String model = "gpt-4o-mini";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
    }

    @Data
    public static class QwenConfig {
        private String apiKey;
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String model = "qwen-plus";
        private Double temperature = 0.7;
        private Integer maxTokens = 2000;
    }

    @Data
    public static class Fallback {
        private boolean enabled = true;
        private int maxRetries = 2;
        private long retryDelayMs = 1000;
    }

    public enum Provider {
        OPENAI,
        DEEPSEEK,
        QWEN
    }
}
