package com.yuexiang.shop.service;

import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.shop.domain.dto.ReviewCreateDTO;
import com.yuexiang.shop.domain.vo.ReviewSummaryVO;
import com.yuexiang.shop.domain.vo.ReviewVO;

public interface ReviewService {

    PageResult<ReviewVO> getShopReviews(Long shopId, Integer sortBy, Integer pageNo, Integer pageSize, Long currentUserId);

    ReviewSummaryVO getReviewSummary(Long shopId);

    void createReview(ReviewCreateDTO dto, Long userId);
}
