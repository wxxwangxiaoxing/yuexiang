package com.yuexiang.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.ai.domain.dto.AiSendMessageDTO;
import com.yuexiang.ai.domain.entity.AiMessage;
import com.yuexiang.ai.domain.entity.AiSession;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import com.yuexiang.ai.domain.vo.RecommendedShopVO;
import com.yuexiang.ai.service.AiContextService;
import com.yuexiang.ai.service.AiConversationService;
import com.yuexiang.ai.service.AiMessageService;
import com.yuexiang.ai.service.AiSessionService;
import com.yuexiang.ai.service.AiSseService;
import com.yuexiang.shop.api.ShopReadService;
import com.yuexiang.shop.domain.vo.NearbyShopPageVO;
import com.yuexiang.shop.domain.vo.NearbyShopVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private static final int RECOMMEND_SHOP_LIMIT = 3;
    private static final int NEARBY_FETCH_SIZE = 6;
    private static final int CONTEXT_MESSAGE_LIMIT = 20;

    private final AiSessionService aiSessionService;
    private final AiMessageService aiMessageService;
    private final AiContextService aiContextService;
    private final AiSseService aiSseService;
    private final com.yuexiang.framework.ai.service.AiChatService aiChatService;
    private final ObjectMapper objectMapper;
    private final ShopReadService shopReadService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSessionDetailVO sendMessage(Long userId, AiSendMessageDTO dto) {
        AiSession session = aiSessionService.getOrCreateSession(userId, dto);
        aiMessageService.saveUserMessage(session.getSessionId(), dto.getQuestion());

        List<RecommendedShopVO> recommendedShops = recommendNearbyShops(dto);
        String aiResponse = generateReply(dto, recommendedShops);
        List<Long> recommendedShopIds = recommendedShops.stream().map(RecommendedShopVO::getShopId).toList();

        aiMessageService.saveAssistantMessage(session.getSessionId(), aiResponse, recommendedShopIds);
        aiContextService.updateContextAfterReply(session, dto.getQuestion(), aiResponse, recommendedShopIds);
        session.setSummary(dto.getQuestion());
        aiSessionService.increaseMessageCount(session, 2, recommendedShopIds);

        return aiSessionService.getSessionDetail(userId, session.getSessionId());
    }

    @Override
    public Flux<String> sendMessageStream(Long userId, AiSendMessageDTO dto) {
        AiSession session = aiSessionService.getOrCreateSession(userId, dto);
        aiMessageService.saveUserMessage(session.getSessionId(), dto.getQuestion());

        List<RecommendedShopVO> recommendedShops = recommendNearbyShops(dto);
        if (!recommendedShops.isEmpty()) {
            String aiResponse = generateReply(dto, recommendedShops);
            List<Long> recommendedShopIds = recommendedShops.stream().map(RecommendedShopVO::getShopId).toList();
            List<String> chunks = splitReply(aiResponse, 32);

            Mono<String> doneEvent = Mono.fromSupplier(() -> {
                aiMessageService.saveAssistantMessage(session.getSessionId(), aiResponse, recommendedShopIds);
                aiContextService.updateContextAfterReply(session, dto.getQuestion(), aiResponse, recommendedShopIds);
                session.setSummary(dto.getQuestion());
                aiSessionService.increaseMessageCount(session, 2, recommendedShopIds);
                return aiSseService.doneEvent(session.getSessionId(), session.getTitle());
            });

            return Flux.concat(
                    Flux.just(aiSseService.sessionEvent(session.getSessionId(), session.getTitle())),
                    Flux.fromIterable(chunks).map(aiSseService::textChunkEvent),
                    doneEvent.flux()
            ).onErrorResume(ex -> {
                log.error("AI流式推荐失败, sessionId={}", session.getSessionId(), ex);
                return Flux.just(aiSseService.errorEvent("AI 服务暂时不可用，请稍后重试"));
            });
        }

        List<AiMessage> recentMessages = aiContextService.loadRecentConversation(session.getSessionId(), CONTEXT_MESSAGE_LIMIT);
        String systemPrompt = aiContextService.buildMergedSystemPrompt(session, recentMessages);
        ChatClient chatClient = aiChatService.createChatClientBuilder()
                .defaultSystem(systemPrompt)
                .build();

        StringBuilder responseBuilder = new StringBuilder();
        Flux<String> contentFlux = chatClient.prompt()
                .user(dto.getQuestion())
                .stream()
                .content()
                .map(chunk -> {
                    responseBuilder.append(chunk);
                    return aiSseService.textChunkEvent(chunk);
                });

        Mono<String> doneEvent = Mono.fromSupplier(() -> {
            String aiResponse = responseBuilder.toString();
            aiMessageService.saveAssistantMessage(session.getSessionId(), aiResponse, List.of());
            aiContextService.updateContextAfterReply(session, dto.getQuestion(), aiResponse, List.of());
            session.setSummary(dto.getQuestion());
            aiSessionService.increaseMessageCount(session, 2, List.of());
            return aiSseService.doneEvent(session.getSessionId(), session.getTitle());
        });

        return Flux.concat(
                Flux.just(aiSseService.sessionEvent(session.getSessionId(), session.getTitle())),
                contentFlux,
                doneEvent.flux()
        ).onErrorResume(ex -> {
            log.error("AI流式对话失败, sessionId={}", session.getSessionId(), ex);
            return Flux.just(aiSseService.errorEvent("AI 服务暂时不可用，请稍后重试"));
        });
    }

    private List<RecommendedShopVO> recommendNearbyShops(AiSendMessageDTO dto) {
        if (dto.getLongitude() == null || dto.getLatitude() == null) {
            return Collections.emptyList();
        }
        try {
            NearbyShopPageVO page = shopReadService.queryNearby(null, dto.getLongitude(), dto.getLatitude(), NEARBY_FETCH_SIZE, null);
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
            log.warn("基于经纬度推荐商户失败, lng={}, lat={}, reason={}", dto.getLongitude(), dto.getLatitude(), ex.getMessage());
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

    private String generateReply(AiSendMessageDTO dto, List<RecommendedShopVO> shops) {
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

    private String fallbackReply(AiSendMessageDTO dto) {
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            return "我已经拿到你的位置信息了，但当前附近没有筛出合适的商户。你可以告诉我预算、口味或场景，我再继续帮你缩小范围。";
        }
        return "告诉我你现在大概在哪个商圈，或者授权定位，我就能直接基于附近真实商户给你推荐。";
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
}
