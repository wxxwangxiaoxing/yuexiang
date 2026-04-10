package com.yuexiang.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.ai.domain.dto.AiChatDTO;
import com.yuexiang.ai.domain.entity.AiMessage;
import com.yuexiang.ai.domain.entity.AiSession;
import com.yuexiang.ai.domain.vo.AiMessageVO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import com.yuexiang.ai.domain.vo.RecommendedShopVO;
import com.yuexiang.ai.mapper.AiMessageMapper;
import com.yuexiang.ai.mapper.AiSessionMapper;
import com.yuexiang.ai.service.AiShopService;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.framework.ai.service.AiChatService;
import com.yuexiang.shop.api.ShopReadService;
import com.yuexiang.shop.domain.vo.NearbyShopPageVO;
import com.yuexiang.shop.domain.vo.NearbyShopVO;
import com.yuexiang.shop.domain.vo.ShopRecommendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiShopServiceImpl implements AiShopService {

    private static final int RECOMMEND_SHOP_LIMIT = 3;
    private static final int NEARBY_FETCH_SIZE = 6;

    private static final String SYSTEM_PROMPT = """
            你是「悦享生活」APP 的 AI 探店助手。

            【核心能力】
            1. 根据用户的口味偏好、预算、位置、场景，推荐合适的商户
            2. 基于真实的探店笔记和评价数据提供推荐理由
            3. 查询并展示商户的优惠券信息
            4. 搜索相关探店笔记供用户参考
            5. 进行自然的多轮对话，主动追问细化需求

            【行为规范】
            1. 只推荐数据库中真实存在的商户，不编造
            2. 推荐理由必须基于真实笔记和评价数据
            3. 价格信息使用"人均XX元"格式
            4. 距离信息基于用户坐标或指定地点计算
            5. 每次推荐 2~5 家商户
            6. 回答简洁友好，适当使用 emoji
            7. 主动询问是否需要查看优惠券、更多详情等
            8. 非探店相关问题，礼貌引导回探店话题

            【实体修正规则】
            当用户说"更便宜的""换个近一点的"时，保留已有实体并调整对应字段。

            【地点处理规则】
            当用户提到具体地名时，调用 search_shops 函数并传入 location 字段。
            """;

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageMapper aiMessageMapper;
    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper;
    private final ShopReadService shopReadService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSessionDetailVO chat(Long userId, AiChatDTO dto) {
        AiSession session = getOrCreateSession(userId, dto);
        saveUserMessage(session.getSessionId(), dto.getQuestion());

        List<RecommendedShopVO> recommendedShops = recommendNearbyShops(dto);
        String aiResponse = generateReply(dto, recommendedShops);
        List<Long> recommendedShopIds = recommendedShops.stream().map(RecommendedShopVO::getShopId).toList();

        saveAssistantMessage(session, aiResponse, recommendedShopIds);
        increaseMessageCount(session, 2, recommendedShopIds);

        return buildSessionDetail(session, loadMessageVOs(session.getSessionId()), recommendedShops);
    }

    @Override
    public Flux<String> chatStream(Long userId, AiChatDTO dto) {
        AiSession session = getOrCreateSession(userId, dto);
        saveUserMessage(session.getSessionId(), dto.getQuestion());

        List<RecommendedShopVO> recommendedShops = recommendNearbyShops(dto);
        if (!recommendedShops.isEmpty()) {
            String aiResponse = generateReply(dto, recommendedShops);
            List<Long> recommendedShopIds = recommendedShops.stream().map(RecommendedShopVO::getShopId).toList();
            List<String> chunks = splitReply(aiResponse, 32);

            Mono<String> doneEvent = Mono.fromSupplier(() -> {
                saveAssistantMessage(session, aiResponse, recommendedShopIds);
                increaseMessageCount(session, 2, recommendedShopIds);
                return toSse("done", Map.of(
                        "sessionId", session.getSessionId(),
                        "title", session.getTitle()
                ));
            });

            return Flux.concat(
                    Flux.just(toSse("session", Map.of(
                            "sessionId", session.getSessionId(),
                            "title", session.getTitle()
                    ))),
                    Flux.fromIterable(chunks).map(chunk -> toSse("delta", Map.of("content", chunk))),
                    doneEvent.flux()
            ).onErrorResume(ex -> {
                log.error("AI流式推荐失败, sessionId={}", session.getSessionId(), ex);
                return Flux.just(toSse("error", Map.of("msg", "AI 服务暂时不可用，请稍后重试")));
            });
        }

        ChatClient chatClient = aiChatService.createChatClientBuilder()
                .defaultSystem(SYSTEM_PROMPT)
                .build();

        StringBuilder responseBuilder = new StringBuilder();

        Flux<String> contentFlux = chatClient.prompt()
                .user(dto.getQuestion())
                .stream()
                .content()
                .map(chunk -> {
                    responseBuilder.append(chunk);
                    return toSse("delta", Map.of("content", chunk));
                });

        Mono<String> doneEvent = Mono.fromSupplier(() -> {
            String aiResponse = responseBuilder.toString();
            saveAssistantMessage(session, aiResponse, List.of());
            increaseMessageCount(session, 2, List.of());
            return toSse("done", Map.of(
                    "sessionId", session.getSessionId(),
                    "title", session.getTitle()
            ));
        });

        return Flux.concat(
                Flux.just(toSse("session", Map.of(
                        "sessionId", session.getSessionId(),
                        "title", session.getTitle()
                ))),
                contentFlux,
                doneEvent.flux()
        ).onErrorResume(ex -> {
            log.error("AI流式对话失败, sessionId={}", session.getSessionId(), ex);
            return Flux.just(toSse("error", Map.of("msg", "AI 服务暂时不可用，请稍后重试")));
        });
    }

    @Override
    public AiSessionDetailVO getSessionDetail(String sessionId, Long userId) {
        AiSession session = aiSessionMapper.selectOne(
                new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getSessionId, sessionId)
                        .eq(AiSession::getUserId, userId)
        );
        if (session == null) {
            return null;
        }

        return buildSessionDetail(session, loadMessageVOs(sessionId), loadRecommendedShops(parseShopIds(session.getShopIds())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(String sessionId, Long userId) {
        AiSession session = aiSessionMapper.selectOne(
                new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getSessionId, sessionId)
                        .eq(AiSession::getUserId, userId)
        );
        if (session == null) {
            return false;
        }

        aiSessionMapper.deleteById(session.getId());
        return true;
    }

    private List<AiMessageVO> loadMessageVOs(String sessionId) {
        List<AiMessage> messages = aiMessageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getSessionId, sessionId)
                        .orderByAsc(AiMessage::getCreateTime)
        );
        return messages.stream().map(this::convertToMessageVO).toList();
    }

    private AiSessionDetailVO buildSessionDetail(AiSession session, List<AiMessageVO> messageVOs,
                                                 List<RecommendedShopVO> shops) {
        return AiSessionDetailVO.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .messageCount(session.getMessageCount())
                .messages(messageVOs)
                .shops(shops)
                .createTime(toTimestamp(session.getCreateTime()))
                .build();
    }

    private List<RecommendedShopVO> recommendNearbyShops(AiChatDTO dto) {
        if (dto.getLng() == null || dto.getLat() == null) {
            return Collections.emptyList();
        }
        try {
            NearbyShopPageVO page = shopReadService.queryNearby(null, dto.getLng(), dto.getLat(), NEARBY_FETCH_SIZE, null);
            List<NearbyShopVO> records = page != null ? page.getRecords() : Collections.emptyList();
            if (records == null || records.isEmpty()) {
                return Collections.emptyList();
            }

            return records.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator
                            .comparing((NearbyShopVO shop) -> shop.getScore() != null ? shop.getScore() : BigDecimal.ZERO, Comparator.reverseOrder())
                            .thenComparing(shop -> shop.getDistance() != null ? shop.getDistance() : Integer.MAX_VALUE))
                    .limit(RECOMMEND_SHOP_LIMIT)
                    .map(this::toRecommendedShopVO)
                    .toList();
        } catch (Exception ex) {
            log.warn("基于经纬度推荐商户失败, lng={}, lat={}, reason={}", dto.getLng(), dto.getLat(), ex.getMessage());
            return Collections.emptyList();
        }
    }

    private RecommendedShopVO toRecommendedShopVO(NearbyShopVO shop) {
        String typeName = null;
        String reason = buildFallbackReason(shop, typeName);

        return RecommendedShopVO.builder()
                .shopId(shop.getId())
                .name(shop.getName())
                .image(firstImage(shop.getImages()))
                .typeName(typeName)
                .score(shop.getScore())
                .avgPrice(shop.getAvgPrice() != null ? shop.getAvgPrice() / 100 : null)
                .distance(shop.getDistance() != null ? shop.getDistance().doubleValue() : null)
                .address(shop.getAddress())
                .reason(reason)
                .build();
    }

    private String buildFallbackReason(NearbyShopVO shop, String typeName) {
        List<String> parts = new ArrayList<>();
        if (typeName != null && !typeName.isBlank()) {
            parts.add(typeName);
        }
        if (shop.getScore() != null) {
            parts.add("评分 " + shop.getScore().setScale(1, RoundingMode.HALF_UP));
        }
        if (shop.getDistance() != null) {
            parts.add(shop.getDistance() + " 米内可达");
        }
        if (shop.getAvgPrice() != null) {
            parts.add("人均 " + (shop.getAvgPrice() / 100) + " 元");
        }
        return parts.isEmpty() ? "附近真实可达商户" : String.join("，", parts);
    }

    private String generateReply(AiChatDTO dto, List<RecommendedShopVO> shops) {
        if (shops.isEmpty()) {
            return fallbackReply(dto);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("我根据你当前位置附近的真实商户数据，先给你推荐这 ")
                .append(shops.size())
                .append(" 家店：\n\n");

        for (int i = 0; i < shops.size(); i++) {
            RecommendedShopVO shop = shops.get(i);
            builder.append(i + 1).append(". ").append(shop.getName());
            if (shop.getTypeName() != null && !shop.getTypeName().isBlank()) {
                builder.append(" | ").append(shop.getTypeName());
            }
            if (shop.getDistance() != null) {
                builder.append(" | 约 ").append(shop.getDistance().intValue()).append(" 米");
            }
            if (shop.getAvgPrice() != null) {
                builder.append(" | 人均 ").append(shop.getAvgPrice()).append(" 元");
            }
            if (shop.getScore() != null) {
                builder.append(" | 评分 ").append(shop.getScore().setScale(1, RoundingMode.HALF_UP));
            }
            builder.append("\n   ").append(shop.getReason()).append("\n\n");
        }

        builder.append("如果你告诉我预算、口味偏好，或者是约会 / 聚餐 / 一个人吃饭这种场景，我可以继续帮你缩小范围。");
        return builder.toString().trim();
    }

    private String fallbackReply(AiChatDTO dto) {
        if (dto.getLng() != null && dto.getLat() != null) {
            return "我已经拿到你的位置信息了，但当前附近没有筛出合适的商户。你可以告诉我预算、口味或场景，我再继续帮你缩小范围。";
        }
        return "告诉我你现在大概在哪个商圈，或者授权定位，我就能直接基于附近真实商户给你推荐。";
    }

    private List<RecommendedShopVO> loadRecommendedShops(List<Long> shopIds) {
        if (shopIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShopRecommendVO> shops = shopReadService.listRecommendShopsByIds(shopIds);
        if (shops == null || shops.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, ShopRecommendVO> shopMap = new HashMap<>(shops.size());
        for (ShopRecommendVO shop : shops) {
            shopMap.put(shop.getShopId(), shop);
        }

        return shopIds.stream()
                .map(shopMap::get)
                .filter(Objects::nonNull)
                .map(shop -> RecommendedShopVO.builder()
                        .shopId(shop.getShopId())
                        .name(shop.getName())
                        .image(shop.getImage())
                        .typeName(shop.getTypeName())
                        .score(shop.getScore())
                        .avgPrice(shop.getAvgPrice())
                        .distance(shop.getDistance())
                        .address(shop.getAddress())
                        .reason(loadReason(shop))
                        .build())
                .toList();
    }

    private String loadReason(ShopRecommendVO shop) {
        NearbyShopVO nearbyShopVO = new NearbyShopVO();
        nearbyShopVO.setId(shop.getShopId());
        nearbyShopVO.setName(shop.getName());
        nearbyShopVO.setAddress(shop.getAddress());
        nearbyShopVO.setAvgPrice(shop.getAvgPrice() != null ? shop.getAvgPrice() * 100 : null);
        nearbyShopVO.setScore(shop.getScore());
        nearbyShopVO.setDistance(shop.getDistance() != null ? shop.getDistance().intValue() : null);
        return buildFallbackReason(nearbyShopVO, shop.getTypeName());
    }

    private List<Long> parseShopIds(String shopIdsJson) {
        if (shopIdsJson == null || shopIdsJson.isBlank() || "[]".equals(shopIdsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(shopIdsJson, new TypeReference<List<Long>>() {});
        } catch (Exception ex) {
            log.warn("解析推荐商户ID失败: {}", shopIdsJson, ex);
            return Collections.emptyList();
        }
    }

    private AiSession getOrCreateSession(Long userId, AiChatDTO dto) {
        String sessionId = dto.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            AiSession session = new AiSession();
            session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
            session.setUserId(userId);
            session.setTitle(dto.getQuestion().length() > 50 ? dto.getQuestion().substring(0, 50) : dto.getQuestion());
            session.setMessageCount(0);
            session.setShopIds("[]");
            aiSessionMapper.insert(session);
            return session;
        }

        AiSession session = aiSessionMapper.selectOne(
                new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getSessionId, sessionId)
                        .eq(AiSession::getUserId, userId)
        );
        if (session == null) {
            throw new NotFoundException("会话不存在");
        }
        return session;
    }

    private void saveUserMessage(String sessionId, String question) {
        AiMessage userMessage = new AiMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(question);
        aiMessageMapper.insert(userMessage);
    }

    private void saveAssistantMessage(AiSession session, String content, List<Long> recommendedShopIds) {
        AiMessage assistantMessage = new AiMessage();
        assistantMessage.setSessionId(session.getSessionId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(content);
        assistantMessage.setShopIds(listToJson(recommendedShopIds));
        aiMessageMapper.insert(assistantMessage);
        session.setShopIds(listToJson(recommendedShopIds));
    }

    private void increaseMessageCount(AiSession session, int increment, List<Long> recommendedShopIds) {
        session.setMessageCount(session.getMessageCount() + increment);
        session.setShopIds(listToJson(recommendedShopIds));
        aiSessionMapper.updateById(session);
    }

    private String firstImage(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) {
            return "";
        }
        try {
            List<String> images = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            return images.isEmpty() ? "" : images.get(0);
        } catch (Exception ex) {
            return imagesJson;
        }
    }

    private List<String> splitReply(String text, int chunkSize) {
        if (text == null || text.isBlank()) {
            return List.of("");
        }
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < text.length(); start += chunkSize) {
            chunks.add(text.substring(start, Math.min(text.length(), start + chunkSize)));
        }
        return chunks;
    }

    private String toSse(String event, Object data) {
        try {
            return "event: " + event + "\n" + "data: " + objectMapper.writeValueAsString(data) + "\n\n";
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("SSE 序列化失败", e);
        }
    }

    private String listToJson(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ids.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private AiMessageVO convertToMessageVO(AiMessage message) {
        return AiMessageVO.builder()
                .messageId(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .shopIds(message.getShopIds())
                .createTime(toTimestamp(message.getCreateTime()))
                .build();
    }

    private Long toTimestamp(LocalDateTime time) {
        if (time == null) return null;
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}


