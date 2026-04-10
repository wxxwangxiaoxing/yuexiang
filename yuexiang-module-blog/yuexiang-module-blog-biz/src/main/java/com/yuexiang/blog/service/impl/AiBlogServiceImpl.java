package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.blog.domain.dto.AiExpandDTO;
import com.yuexiang.blog.domain.dto.AiPolishDTO;
import com.yuexiang.blog.domain.dto.AiTagsDTO;
import com.yuexiang.blog.domain.dto.AiTitleDTO;
import com.yuexiang.blog.domain.entity.Tag;
import com.yuexiang.blog.domain.vo.AiExpandVO;
import com.yuexiang.blog.domain.vo.AiPolishVO;
import com.yuexiang.blog.domain.vo.AiTagsVO;
import com.yuexiang.blog.domain.vo.AiTitleVO;
import com.yuexiang.blog.mapper.BlogTagMapper;
import com.yuexiang.blog.service.AiBlogService;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.framework.ai.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "yuexiang.ai", name = "enabled", havingValue = "true")
public class AiBlogServiceImpl implements AiBlogService {

    private final BlogTagMapper tagMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AiChatService aiChatService;

    // ==================== Redis Key 前缀 ====================
    private static final String AI_LIMIT_TITLE_PREFIX = "ai:limit:title:";
    private static final String AI_LIMIT_POLISH_PREFIX = "ai:limit:polish:";
    private static final String AI_LIMIT_EXPAND_PREFIX = "ai:limit:expand:";
    private static final String AI_LIMIT_TAGS_PREFIX = "ai:limit:tags:";
    private static final String AI_LIMIT_TOTAL_PREFIX = "ai:limit:total:";

    // ==================== 每日限额 ====================
    private static final int TITLE_DAILY_LIMIT = 20;
    private static final int POLISH_DAILY_LIMIT = 15;
    private static final int EXPAND_DAILY_LIMIT = 10;
    private static final int TAGS_DAILY_LIMIT = 20;
    private static final int TOTAL_DAILY_LIMIT = 50;

    // ==================== 魔法值常量化 ====================
    private static final int MAX_TITLE_LENGTH = 30;
    private static final int MAX_TITLE_COUNT = 3;
    private static final int MAX_TAG_COUNT = 5;
    private static final int FALLBACK_TITLE_MAX_LENGTH = 20;

    // ==================== 风格 & 目标字数映射（消除 switch） ====================
    private static final Map<String, String> STYLE_MAP = Map.of(
            "literary", "文艺清新",
            "professional", "专业客观",
            "humorous", "幽默风趣"
    );
    private static final String DEFAULT_STYLE = "活泼生动";

    private static final Map<String, Integer> TARGET_LENGTH_MAP = Map.of(
            "short", 200,
            "long", 650
    );
    private static final int DEFAULT_TARGET_WORDS = 400;

    // ==================== System Prompts ====================
    private static final String TITLE_SYSTEM_PROMPT = """
            你是探店笔记标题生成助手。
            根据用户提供的笔记内容，生成3个吸引人的标题。
            要求：
            1. 每个标题不超过30字
            2. 风格活泼生动
            3. 仅返回标题，每行一个，不要编号
            """;

    private static final String POLISH_SYSTEM_PROMPT = """
            你是探店笔记润色助手。
            根据用户指定的风格优化笔记内容。
            要求：
            1. 保持原意
            2. 直接返回润色后的内容，不要解释
            """;

    private static final String EXPAND_SYSTEM_PROMPT = """
            你是探店笔记扩写助手。
            将用户提供的简短描述扩写为丰富的探店笔记。
            要求：
            1. 保持原意和风格
            2. 增加细节描写
            3. 直接返回扩写后的内容
            """;

    private static final String TAGS_SYSTEM_PROMPT = """
            你是探店笔记标签提取助手。
            从笔记内容中提取3-5个标签关键词。
            标签类型包括：菜系、地域、场景、价格区间、特色亮点。
            要求：
            1. 仅返回标签词
            2. 用逗号分隔
            3. 不要编号和解释
            """;

    // ==================== 业务方法 ====================

    @Override
    public AiTitleVO generateTitle(AiTitleDTO dto, Long userId) {
        checkRateLimitIfNeeded(AI_LIMIT_TITLE_PREFIX, TITLE_DAILY_LIMIT, userId);

        List<String> titles = Collections.emptyList();
        boolean fallback = false;

        try {
            String prompt = "请为以下探店笔记内容生成标题：\n\n" + dto.getContent();
            String result = aiChatService.chat(prompt, TITLE_SYSTEM_PROMPT);
            titles = parseTitles(result);
        } catch (Exception e) {
            log.warn("AI生成标题失败, userId={}: {}", userId, e.getMessage());
            fallback = true;
        }

        if (titles.isEmpty()) {
            titles = List.of(generateFallbackTitle(dto.getContent()));
            fallback = true;
        }

        // 【修复】成功时才计数，且同时递增分类计数器
        if (!fallback) {
            incrementCountersSafely(AI_LIMIT_TITLE_PREFIX, userId);
        }

        AiTitleVO vo = new AiTitleVO();
        vo.setTitles(titles);
        vo.setFallback(fallback);
        return vo;
    }

    @Override
    public AiPolishVO polishContent(AiPolishDTO dto, Long userId) {
        checkRateLimitIfNeeded(AI_LIMIT_POLISH_PREFIX, POLISH_DAILY_LIMIT, userId);

        String polished = dto.getContent();
        boolean fallback = false;

        try {
            String styleDesc = STYLE_MAP.getOrDefault(dto.getStyle(), DEFAULT_STYLE);
            String prompt = String.format("请以【%s】的风格润色以下内容：\n\n%s", styleDesc, dto.getContent());
            String result = aiChatService.chat(prompt, POLISH_SYSTEM_PROMPT);
            if (StringUtils.hasText(result)) {
                polished = result;
            }
        } catch (Exception e) {
            log.warn("AI润色失败, userId={}: {}", userId, e.getMessage());
            fallback = true;
        }

        if (!fallback) {
            incrementCountersSafely(AI_LIMIT_POLISH_PREFIX, userId);
        }

        AiPolishVO vo = new AiPolishVO();
        vo.setOriginal(dto.getContent());
        vo.setPolished(polished);
        vo.setChanges(Collections.emptyList());
        vo.setFallback(fallback);
        return vo;
    }

    @Override
    public AiExpandVO expandContent(AiExpandDTO dto, Long userId) {
        checkRateLimitIfNeeded(AI_LIMIT_EXPAND_PREFIX, EXPAND_DAILY_LIMIT, userId);

        String expanded = dto.getContent();
        boolean fallback = false;

        try {
            int targetWords = TARGET_LENGTH_MAP.getOrDefault(dto.getTargetLength(), DEFAULT_TARGET_WORDS);
            String prompt = String.format("请将以下内容扩写为%d字左右的丰富笔记：\n\n%s", targetWords, dto.getContent());
            String result = aiChatService.chat(prompt, EXPAND_SYSTEM_PROMPT);
            if (StringUtils.hasText(result)) {
                expanded = result;
            }
        } catch (Exception e) {
            log.warn("AI扩写失败, userId={}: {}", userId, e.getMessage());
            fallback = true;
        }

        if (!fallback) {
            incrementCountersSafely(AI_LIMIT_EXPAND_PREFIX, userId);
        }

        AiExpandVO vo = new AiExpandVO();
        vo.setOriginal(dto.getContent());
        vo.setExpanded(expanded);
        vo.setWordCount(expanded.length());
        vo.setFallback(fallback);
        return vo;
    }

    @Override
    public AiTagsVO suggestTags(AiTagsDTO dto, Long userId) {
        checkRateLimitIfNeeded(AI_LIMIT_TAGS_PREFIX, TAGS_DAILY_LIMIT, userId);
        List<String> tagNames;
        boolean fallback = false;
        String combinedContent = buildCombinedContent(dto.getTitle(), dto.getContent());
        try {
            String prompt = "请从以下探店笔记中提取标签：\n\n" + combinedContent;
            String result = aiChatService.chat(prompt, TAGS_SYSTEM_PROMPT);
            tagNames = parseTags(result);
            if (tagNames.isEmpty()) {
                tagNames = extractKeywords(combinedContent);
                fallback = true;
            }
        } catch (Exception e) {
            log.warn("AI打标签失败, userId={}: {}", userId, e.getMessage());
            fallback = true;
            tagNames = extractKeywords(combinedContent);
        }

        // 【优化】批量查询替代循环单条查询
        List<AiTagsVO.TagVO> tags = matchTagsFromDb(tagNames);

        if (!fallback) {
            incrementCountersSafely(AI_LIMIT_TAGS_PREFIX, userId);
        }

        AiTagsVO vo = new AiTagsVO();
        vo.setTags(tags);
        vo.setFallback(fallback);
        return vo;
    }

    // ==================== 限流相关 ====================

    /**
     * 统一入口：userId 为空则跳过限流检查
     */
    private void checkRateLimitIfNeeded(String prefix, int limit, Long userId) {
        if (userId == null) {
            return;
        }
        // 总量检查
        int totalUsed = getRedisCounter(AI_LIMIT_TOTAL_PREFIX + userId);
        if (totalUsed >= TOTAL_DAILY_LIMIT) {
            throw new BadRequestException("AI使用次数已达今日上限（" + TOTAL_DAILY_LIMIT + "次）");
        }
        // 分类检查
        int typeUsed = getRedisCounter(prefix + userId);
        if (typeUsed >= limit) {
            throw new BadRequestException("该AI功能使用次数已达今日上限（" + limit + "次）");
        }
    }

    /**
     * 【修复】同时递增总量 + 分类计数器，并安全设置过期时间
     */
    private void incrementCountersSafely(String typePrefix, Long userId) {
        if (userId == null) {
            return;
        }
        try {
            long ttlSeconds = getSecondsUntilMidnight();
            incrementWithExpiry(AI_LIMIT_TOTAL_PREFIX + userId, ttlSeconds);
            incrementWithExpiry(typePrefix + userId, ttlSeconds);
        } catch (Exception e) {
            log.warn("更新AI使用计数失败, userId={}: {}", userId, e.getMessage());
        }
    }

    private int getRedisCounter(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Redis计数器值异常, key={}, value={}", key, value);
            return 0;
        }
    }

    /**
     * 【优化】使用 increment 原子操作替代 get-then-set，避免并发竞态
     * 仅在 key 首次创建（值为1）时设置过期时间
     */
    private void incrementWithExpiry(String key, long ttlSeconds) {
        Long newValue = stringRedisTemplate.opsForValue().increment(key);
        if (newValue != null && newValue == 1L) {
            stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 【修复】使用次日零点计算，原代码用 LocalTime.MAX（23:59:59.999）会少约1秒
     */
    private long getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        return Math.max(Duration.between(now, midnight).getSeconds(), 1L);
    }

    // ==================== 解析相关 ====================

    private List<String> parseTitles(String result) {
        if (!StringUtils.hasText(result)) {
            return Collections.emptyList();
        }
        return Arrays.stream(result.split("[\\n,，]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.length() <= MAX_TITLE_LENGTH)
                .limit(MAX_TITLE_COUNT)
                .collect(Collectors.toList());
    }

    private List<String> parseTags(String result) {
        if (!StringUtils.hasText(result)) {
            return Collections.emptyList();
        }
        return Arrays.stream(result.split("[,，\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()    // 【优化】去重
                .limit(MAX_TAG_COUNT)
                .collect(Collectors.toList());
    }

    // ==================== 降级 / Fallback ====================

    private String generateFallbackTitle(String content) {
        if (!StringUtils.hasText(content)) {
            return "探店分享";
        }
        String cleaned = content.replaceAll("\\s+", "");
        int len = Math.min(cleaned.length(), FALLBACK_TITLE_MAX_LENGTH);
        return cleaned.substring(0, len) + " - 探店分享";
    }

    private List<String> extractKeywords(String content) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptyList();
        }
        // TODO: 后续可接入分词工具（如 HanLP/jieba）实现真正的关键词提取
        return List.of("美食", "探店", "推荐");
    }

    // ==================== 数据库操作 ====================

    /**
     * 【优化】批量 IN 查询替代 N+1 循环查询，减少数据库压力
     */
    private List<AiTagsVO.TagVO> matchTagsFromDb(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptyList();
        }

        // 一次性查出所有匹配的标签
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Tag::getName, tagNames)
                .eq(Tag::getType, 1)
                .eq(Tag::getDeleted, 0);
        List<Tag> dbTags = tagMapper.selectList(wrapper);

        // 构建 name -> Tag 映射，O(1) 查找
        Map<String, Tag> tagMap = dbTags.stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity(), (a, b) -> a));

        // 保持原始顺序组装结果
        return tagNames.stream()
                .map(name -> buildTagVO(name, tagMap.get(name)))
                .collect(Collectors.toList());
    }

    private AiTagsVO.TagVO buildTagVO(String name, Tag tag) {
        AiTagsVO.TagVO tagVO = new AiTagsVO.TagVO();
        tagVO.setName(name);
        if (tag != null) {
            tagVO.setId(tag.getId());
            tagVO.setMatched(true);
        } else {
            tagVO.setId(null);
            tagVO.setMatched(false);
        }
        return tagVO;
    }

    // ==================== 工具方法 ====================

    private String buildCombinedContent(String title, String content) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(title)) {
            sb.append(title);
        }
        if (StringUtils.hasText(content)) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(content);
        }
        return sb.toString();
    }
}