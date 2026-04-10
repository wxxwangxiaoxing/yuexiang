package com.yuexiang.framework.storage.config;

import com.yuexiang.framework.storage.service.StorageService;
import com.yuexiang.framework.storage.service.impl.LocalStorageServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageAutoConfiguration {

    @Bean
    public StorageService storageService() {
        return new LocalStorageServiceImpl();
    }
}
