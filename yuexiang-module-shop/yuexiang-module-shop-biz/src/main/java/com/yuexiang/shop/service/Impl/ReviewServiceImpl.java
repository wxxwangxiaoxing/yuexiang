package com.yuexiang.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.NotFoundException;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.shop.domain.dto.ReviewCreateDTO;
import com.yuexiang.shop.domain.entity.Review;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.vo.ReviewSummaryVO;
import com.yuexiang.shop.domain.vo.ReviewVO;
import com.yuexiang.shop.mapper.ReviewMapper;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final ShopMapper shopMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<ReviewVO> getShopReviews(Long shopId, Integer sortBy, Integer pageNo, Integer pageSize, Long currentUserId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getShopId, shopId)
                .eq(Review::getStatus, 1)
                .eq(Review::getDeleted, 0);

        if (sortBy != null && sortBy == 1) {
            wrapper.orderByDesc(Review::getLikeCount);
        } else {
            wrapper.orderByDesc(Review::getCreateTime);
        }

        Page<Review> page = reviewMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);

        List<ReviewVO> records = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, page.getTotal());
    }

    @Override
    public ReviewSummaryVO getReviewSummary(Long shopId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getShopId, shopId)
                .eq(Review::getStatus, 1)
                .eq(Review::getDeleted, 0);

        List<Review> reviews = reviewMapper.selectList(wrapper);
        if (reviews.isEmpty()) {
            ReviewSummaryVO vo = new ReviewSummaryVO();
            vo.setTotalReviews(0);
            vo.setAvgScore(0.0);
            vo.setScore5Count(0);
            vo.setScore4Count(0);
            vo.setScore3Count(0);
            vo.setScore2Count(0);
            vo.setScore1Count(0);
            return vo;
        }

        ReviewSummaryVO vo = new ReviewSummaryVO();
        vo.setTotalReviews(reviews.size());
        vo.setAvgScore(round(reviews.stream().mapToInt(Review::getScore).average().orElse(0.0)));
        vo.setAvgScoreTaste(round(reviews.stream().filter(r -> r.getScoreTaste() != null).mapToInt(Review::getScoreTaste).average().orElse(0.0)));
        vo.setAvgScoreEnv(round(reviews.stream().filter(r -> r.getScoreEnv() != null).mapToInt(Review::getScoreEnv).average().orElse(0.0)));
        vo.setAvgScoreService(round(reviews.stream().filter(r -> r.getScoreService() != null).mapToInt(Review::getScoreService).average().orElse(0.0)));
        vo.setScore5Count((int) reviews.stream().filter(r -> r.getScore() == 5).count());
        vo.setScore4Count((int) reviews.stream().filter(r -> r.getScore() == 4).count());
        vo.setScore3Count((int) reviews.stream().filter(r -> r.getScore() == 3).count());
        vo.setScore2Count((int) reviews.stream().filter(r -> r.getScore() == 2).count());
        vo.setScore1Count((int) reviews.stream().filter(r -> r.getScore() == 1).count());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReview(ReviewCreateDTO dto, Long userId) {
        Shop shop = shopMapper.selectById(dto.getShopId());
        if (shop == null || shop.getDeleted() == 1) {
            throw new NotFoundException("商户不存在");
        }

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
                .eq(Review::getShopId, dto.getShopId())
                .eq(Review::getDeleted, 0);
        if (reviewMapper.selectCount(wrapper) > 0) {
            throw new BadRequestException("您已评价过该商户");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setShopId(dto.getShopId());
        review.setOrderId(dto.getOrderId());
        review.setScore(dto.getScore());
        review.setScoreTaste(dto.getScoreTaste());
        review.setScoreEnv(dto.getScoreEnv());
        review.setScoreService(dto.getScoreService());
        review.setContent(dto.getContent());
        review.setImages(toJsonString(dto.getImages()));
        review.setLikeCount(0);
        review.setStatus(1);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        review.setDeleted(0);
        reviewMapper.insert(review);

        log.info("用户提交评价: userId={}, shopId={}, score={}", userId, dto.getShopId(), dto.getScore());
    }

    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setUserId(review.getUserId());
        vo.setNickName("用户" + review.getUserId());
        vo.setAvatar(null);
        vo.setScore(review.getScore());
        vo.setScoreTaste(review.getScoreTaste());
        vo.setScoreEnv(review.getScoreEnv());
        vo.setScoreService(review.getScoreService());
        vo.setContent(review.getContent());
        vo.setImages(parseImages(review.getImages()));
        vo.setLikeCount(review.getLikeCount());
        vo.setIsLiked(false);
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String toJsonString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("图片列表序列化失败", e);
            return null;
        }
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
