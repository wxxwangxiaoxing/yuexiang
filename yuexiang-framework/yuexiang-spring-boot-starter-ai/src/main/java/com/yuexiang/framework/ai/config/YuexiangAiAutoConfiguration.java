package com.yuexiang.framework.ai.config;

import com.yuexiang.framework.ai.service.AiChatService;
import com.yuexiang.framework.ai.service.AiChatServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@ConditionalOnProperty(prefix = "yuexiang.ai", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(YuexiangAiProperties.class)
public class YuexiangAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChatModel chatModel(YuexiangAiProperties properties) {
        YuexiangAiProperties.Provider primary = properties.getPrimary();
        String apiKey = null;
        String baseUrl = null;
        String model = null;
        Double temperature = null;
        Integer maxTokens = null;

        switch (primary) {
            case DEEPSEEK -> {
                YuexiangAiProperties.DeepSeekConfig cfg = properties.getDeepseek();
                apiKey = cfg.getApiKey();
                baseUrl = cfg.getBaseUrl();
                model = cfg.getModel();
                temperature = cfg.getTemperature();
                maxTokens = cfg.getMaxTokens();
            }
            case OPENAI -> {
                YuexiangAiProperties.OpenAiConfig cfg = properties.getOpenai();
                apiKey = cfg.getApiKey();
                baseUrl = cfg.getBaseUrl();
                model = cfg.getModel();
                temperature = cfg.getTemperature();
                maxTokens = cfg.getMaxTokens();
            }
            case QWEN -> {
                YuexiangAiProperties.QwenConfig cfg = properties.getQwen();
                apiKey = cfg.getApiKey();
                baseUrl = cfg.getBaseUrl();
                model = cfg.getModel();
                temperature = cfg.getTemperature();
                maxTokens = cfg.getMaxTokens();
            }
        }

        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your-")) {
            log.warn("[AI配置] 未配置有效的 API Key (primary={})，AI 功能将不可用", primary);
            return null;
        }

        log.info("[AI配置] 初始化 ChatModel: primary={}, model={}, baseUrl={}", primary, model, baseUrl);

        OpenAiApi api = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        return new OpenAiChatModel(api, options);
    }

    @Bean
    @ConditionalOnMissingBean
    public AiChatService aiChatService() {
        log.info("[YuexiangAiAutoConfiguration] 初始化AI聊天服务");
        return new AiChatServiceImpl();
    }
}
