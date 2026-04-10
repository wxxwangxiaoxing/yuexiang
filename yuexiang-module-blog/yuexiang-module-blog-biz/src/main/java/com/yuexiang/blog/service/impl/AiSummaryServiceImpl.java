package com.yuexiang.blog.service.impl;

import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.domain.entity.Blog;
import com.yuexiang.blog.domain.vo.AiSummaryVO;
import com.yuexiang.blog.mapper.BlogMapper;
import com.yuexiang.blog.service.AiSummaryService;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.framework.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "yuexiang.ai", name = "enabled", havingValue = "true")
public class AiSummaryServiceImpl implements AiSummaryService {

    private final BlogMapper blogMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AiChatService aiChatService;   // 【修复】接入真实 AI 服务

    private static final int SUMMARY_MAX_LENGTH = 100;

    // 【优化】摘要内容最大输入长度，防止 token 超限
    private static final int MAX_INPUT_LENGTH = 2000;

    // 【优化】降级摘要也缓存，但 TTL 较短，避免反复触发降级逻辑
    private static final long FALLBACK_CACHE_TTL_SECONDS = 300L;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            你是探店笔记摘要助手。
            根据用户提供的笔记标题和内容，生成一段简洁的摘要。
            要求：
            1. 摘要不超过100字
            2. 突出核心亮点（菜品、环境、价格等）
            3. 直接返回摘要内容，不要任何前缀和解释
            """;

    @Override
    public AiSummaryVO getSummary(Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new NotFoundException("笔记不存在");
        }

        String cacheKey = BlogConstants.BLOG_SUMMARY_KEY_PREFIX + blogId;

        // 1. 查缓存
        String cachedSummary = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cachedSummary)) {
            return buildVO(cachedSummary, true, false);
        }

        // 2. 调用 AI 生成
        String summary = generateSummary(blog);
        boolean isFallback = false;

        // 3. AI 失败则降级
        if (!StringUtils.hasText(summary)) {
            summary = generateFallbackSummary(blog.getContent());
            isFallback = true;
        }

        // 4. 写入缓存（降级结果用较短 TTL）
        cacheSummary(cacheKey, summary, isFallback);

        return buildVO(summary, false, isFallback);
    }

    // ==================== AI 调用 ====================

    private String generateSummary(Blog blog) {
        try {
            String prompt = buildPrompt(blog.getTitle(), blog.getContent());
            String result = aiChatService.chat(prompt, SUMMARY_SYSTEM_PROMPT);
            return truncateIfNeeded(result);
        } catch (Exception e) {
            log.warn("AI生成摘要失败, blogId={}: {}", blog.getId(), e.getMessage());
            return null;
        }
    }

    private String buildPrompt(String title, String content) {
        StringBuilder sb = new StringBuilder("请为以下探店笔记生成摘要：\n\n");
        if (StringUtils.hasText(title)) {
            sb.append("标题：").append(title).append("\n\n");
        }
        if (StringUtils.hasText(content)) {
            // 【优化】截断过长内容，避免 token 超限和费用浪费
            String cleaned = stripTagsAndTrim(content);
            if (cleaned.length() > MAX_INPUT_LENGTH) {
                cleaned = cleaned.substring(0, MAX_INPUT_LENGTH) + "...";
            }
            sb.append("内容：").append(cleaned);
        }
        return sb.toString();
    }

    // ==================== 降级逻辑 ====================

    private String generateFallbackSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "暂无摘要";
        }
        String cleaned = stripTagsAndTrim(content);
        if (cleaned.length() <= SUMMARY_MAX_LENGTH) {
            return cleaned;
        }
        // 【优化】在标点或空格处断句，避免截断在词中间
        return truncateAtNaturalBreak(cleaned, SUMMARY_MAX_LENGTH) + "...";
    }

    // ==================== 缓存 ====================

    private void cacheSummary(String cacheKey, String summary, boolean isFallback) {
        if (!StringUtils.hasText(summary)) {
            return;
        }
        try {
            long ttl = isFallback ? FALLBACK_CACHE_TTL_SECONDS : BlogConstants.BLOG_SUMMARY_TTL;
            stringRedisTemplate.opsForValue().set(cacheKey, summary, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("缓存摘要失败, key={}: {}", cacheKey, e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 去除 HTML 标签、Markdown 标记、多余空白
     */
    private String stripTagsAndTrim(String content) {
        return content
                .replaceAll("<[^>]+>", "")          // HTML 标签
                .replaceAll("!?\\[[^]]*]\\([^)]*\\)", "") // Markdown 图片/链接
                .replaceAll("[#*`>~_|\\-]{1,6}\\s?", "")  // Markdown 标记符
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * 【优化】在最大长度内寻找最近的标点或空格处截断
     */
    private String truncateAtNaturalBreak(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        // 从 maxLength 往前找标点符号
        String breakChars = "。！？；，、.!?; ";
        for (int i = maxLength; i > maxLength - 20 && i > 0; i--) {
            if (breakChars.indexOf(text.charAt(i)) >= 0) {
                return text.substring(0, i + 1);
            }
        }
        return text.substring(0, maxLength);
    }

    /**
     * AI 返回结果可能超长，做安全截断
     */
    private String truncateIfNeeded(String result) {
        if (!StringUtils.hasText(result)) {
            return null;
        }
        String trimmed = result.trim();
        if (trimmed.length() > SUMMARY_MAX_LENGTH) {
            return truncateAtNaturalBreak(trimmed, SUMMARY_MAX_LENGTH) + "...";
        }
        return trimmed;
    }

    /**
     * 统一构建 VO，消除重复代码
     */
    private AiSummaryVO buildVO(String summary, boolean cached, boolean fallback) {
        AiSummaryVO vo = new AiSummaryVO();
        vo.setSummary(summary);
        vo.setCached(cached);
        vo.setFallback(fallback);
        return vo;
    }
}