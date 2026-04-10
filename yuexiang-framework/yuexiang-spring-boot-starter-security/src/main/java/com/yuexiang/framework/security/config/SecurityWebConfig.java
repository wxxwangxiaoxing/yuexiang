package com.yuexiang.framework.security.config;

import com.yuexiang.framework.security.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityWebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public SecurityWebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/doc.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/api/shop/**",
                        "/api/shop-type/**",
                        "/api/voucher/**",
                        "/api/blog/{id}",
                        "/api/blog/*/ai-summary",
                        "/api/blog/*/comments",
                        "/api/blog/comments/*/replies",
                        "/api/ai/blog/**",
                        "/api/shop/search",
                        "/api/tag/suggest"
                );
    }
}
