package com.yuexiang.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.ai.domain.dto.AiCreateSessionDTO;
import com.yuexiang.ai.domain.dto.AiSessionQueryDTO;
import com.yuexiang.ai.domain.dto.AiSendMessageDTO;
import com.yuexiang.ai.domain.entity.AiSession;
import com.yuexiang.ai.domain.vo.AiMessageVO;
import com.yuexiang.ai.domain.vo.AiSessionDetailVO;
import com.yuexiang.ai.domain.vo.AiSessionListItemVO;
import com.yuexiang.ai.domain.vo.RecommendedShopVO;
import com.yuexiang.ai.mapper.AiSessionMapper;
import com.yuexiang.ai.service.AiMessageService;
import com.yuexiang.ai.service.AiSessionService;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.shop.api.ShopReadService;
import com.yuexiang.shop.domain.vo.NearbyShopVO;
import com.yuexiang.shop.domain.vo.ShopRecommendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSessionServiceImpl implements AiSessionService {

    private final AiSessionMapper aiSessionMapper;
    private final AiMessageService aiMessageService;
    private final ShopReadService shopReadService;
    private final ObjectMapper objectMapper;

    @Override
    public AiSessionDetailVO createSession(Long userId, AiCreateSessionDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        AiSession session = new AiSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId);
        session.setTitle("新的探店会话");
        session.setMessageCount(0);
        session.setStatus(1);
        session.setShopIds("[]");
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setLastActiveTime(now);
        session.setTotalTokens(0);
        session.setContextJson("{}");
        if (dto != null) {
            session.setLongitude(toBigDecimal(dto.getLongitude()));
            session.setLatitude(toBigDecimal(dto.getLatitude()));
        }
        aiSessionMapper.insert(session);
        return buildSessionDetail(session, List.of(), Collections.emptyList());
    }

    @Override
    public AiSession getOrCreateSession(Long userId, AiSendMessageDTO dto) {
        String sessionId = dto.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            LocalDateTime now = LocalDateTime.now();
            AiSession session = new AiSession();
            session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
            session.setUserId(userId);
            String title = dto.getQuestion();
            if (title != null && title.length() > 50) {
                title = title.substring(0, 50);
            }
            session.setTitle(title == null || title.isBlank() ? "新的探店会话" : title);
            session.setMessageCount(0);
            session.setStatus(1);
            session.setShopIds("[]");
            session.setCreateTime(now);
            session.setUpdateTime(now);
            session.setLastActiveTime(now);
            session.setTotalTokens(0);
            session.setContextJson("{}");
            session.setLongitude(toBigDecimal(dto.getLongitude()));
            session.setLatitude(toBigDecimal(dto.getLatitude()));
            aiSessionMapper.insert(session);
            return session;
        }

        AiSession session = findSession(userId, sessionId);
        if (session == null) {
            throw new NotFoundException("会话不存在");
        }
        if (dto.getLongitude() != null) {
            session.setLongitude(toBigDecimal(dto.getLongitude()));
        }
        if (dto.getLatitude() != null) {
            session.setLatitude(toBigDecimal(dto.getLatitude()));
        }
        return session;
    }

    @Override
    public AiSession getRequiredSession(Long userId, String sessionId) {
        AiSession session = findSession(userId, sessionId);
        if (session == null) {
            throw new NotFoundException("会话不存在");
        }
        return session;
    }

    @Override
    public AiSessionDetailVO getSessionDetail(Long userId, String sessionId) {
        AiSession session = findSession(userId, sessionId);
        if (session == null) {
            return null;
        }
        return buildSessionDetail(session, aiMessageService.listMessageVOs(sessionId),
                loadRecommendedShops(parseShopIds(session.getShopIds())));
    }

    @Override
    public List<AiMessageVO> getSessionHistory(Long userId, String sessionId) {
        getRequiredSession(userId, sessionId);
        return aiMessageService.listMessageVOs(sessionId);
    }

    @Override
    public PageResult<AiSessionListItemVO> listSessions(Long userId, AiSessionQueryDTO queryDTO) {
        int pageNo = queryDTO == null || queryDTO.getPageNo() == null ? 1 : Math.max(queryDTO.getPageNo(), 1);
        int pageSize = queryDTO == null || queryDTO.getPageSize() == null ? 10 : Math.min(Math.max(queryDTO.getPageSize(), 1), 50);

        Page<AiSession> page = aiSessionMapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getUserId, userId)
                        .orderByDesc(AiSession::getUpdateTime)
                        .orderByDesc(AiSession::getCreateTime));

        List<AiSessionListItemVO> list = page.getRecords().stream()
                .map(this::toSessionListItemVO)
                .toList();
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public boolean deleteSession(Long userId, String sessionId) {
        AiSession session = findSession(userId, sessionId);
        if (session == null) {
            return false;
        }
        aiSessionMapper.deleteById(session.getId());
        return true;
    }

    @Override
    public void increaseMessageCount(AiSession session, int increment, List<Long> recommendedShopIds) {
        LocalDateTime now = LocalDateTime.now();
        session.setMessageCount((session.getMessageCount() == null ? 0 : session.getMessageCount()) + increment);
        session.setShopIds(listToJson(recommendedShopIds));
        session.setUpdateTime(now);
        session.setLastActiveTime(now);
        session.setTotalTokens(session.getTotalTokens() == null ? 0 : session.getTotalTokens());
        aiSessionMapper.updateById(session);
    }

    private AiSession findSession(Long userId, String sessionId) {
        return aiSessionMapper.selectOne(new LambdaQueryWrapper<AiSession>()
                .eq(AiSession::getSessionId, sessionId)
                .eq(AiSession::getUserId, userId));
    }

    private AiSessionDetailVO buildSessionDetail(AiSession session, List<AiMessageVO> messages,
                                                 List<RecommendedShopVO> shops) {
        return AiSessionDetailVO.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .messageCount(session.getMessageCount())
                .status(session.getStatus() == null ? 1 : session.getStatus())
                .lastActiveTime(toTimestamp(session.getLastActiveTime() != null ? session.getLastActiveTime() : session.getUpdateTime()))
                .totalTokens(session.getTotalTokens() == null ? 0 : session.getTotalTokens())
                .summary(session.getSummary())
                .contextJson(session.getContextJson())
                .longitude(session.getLongitude())
                .latitude(session.getLatitude())
                .messages(messages)
                .shops(shops)
                .createTime(toTimestamp(session.getCreateTime()))
                .build();
    }

    private AiSessionListItemVO toSessionListItemVO(AiSession session) {
        return AiSessionListItemVO.builder()
                .sessionId(session.getSessionId())
                .title(session.getTitle())
                .messageCount(session.getMessageCount())
                .status(session.getStatus() == null ? 1 : session.getStatus())
                .totalTokens(session.getTotalTokens() == null ? 0 : session.getTotalTokens())
                .lastActiveTime(toTimestamp(session.getLastActiveTime() != null ? session.getLastActiveTime() : session.getUpdateTime()))
                .createTime(toTimestamp(session.getCreateTime()))
                .longitude(session.getLongitude())
                .latitude(session.getLatitude())
                .build();
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

    private String listToJson(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ids.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private Long toTimestamp(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
