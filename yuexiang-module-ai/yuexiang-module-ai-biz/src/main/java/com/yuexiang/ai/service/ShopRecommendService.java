package com.yuexiang.ai.service;

import com.yuexiang.ai.domain.vo.RecommendedShopVO;
import com.yuexiang.ai.functions.BlogSearchRequest;
import com.yuexiang.ai.functions.BlogSummaryVO;
import com.yuexiang.ai.functions.ShopSearchRequest;
import com.yuexiang.shop.domain.vo.ShopVO;

import java.util.List;

public interface ShopRecommendService {

    List<RecommendedShopVO> searchRecommendedShops(ShopSearchRequest request);

    ShopVO getShopDetail(Long shopId);

    List<BlogSummaryVO> searchBlogs(BlogSearchRequest request);
}
