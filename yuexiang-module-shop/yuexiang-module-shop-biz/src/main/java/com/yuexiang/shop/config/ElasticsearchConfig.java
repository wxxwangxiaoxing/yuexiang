package com.yuexiang.shop.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "yuexiang.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchConfig {
}
