package com.yuexiang.ai.functions;

import com.yuexiang.ai.domain.vo.RecommendedShopVO;
import com.yuexiang.ai.service.ShopRecommendService;
import com.yuexiang.shop.domain.vo.ShopVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiFunctionConfig {

    private final ShopRecommendService shopRecommendService;

    @Bean
    @Description("根据条件搜索推荐商户。当用户提到菜系、预算、地点、场景等需求时调用。当用户说'更便宜''更近'等修正条件时，也调用此函数并调整对应参数。")
    public Function<ShopSearchRequest, List<RecommendedShopVO>> searchShops() {
        return request -> {
            log.info("Function Calling: searchShops with request: {}", request);
            return shopRecommendService.searchRecommendedShops(request);
        };
    }

    @Bean
    @Description("获取商户详细信息，包括营业时间、多维评分、最新评价等。当用户问'怎么样''评价如何''几点关门'时调用。")
    public Function<ShopDetailRequest, ShopVO> getShopDetail() {
        return request -> {
            log.info("Function Calling: getShopDetail with request: {}", request);
            if (request.getShopId() == null) {
                return null;
            }
            return shopRecommendService.getShopDetail(request.getShopId());
        };
    }

    @Bean
    @Description("搜索相关探店笔记。当用户想看真实探店体验、评价详情时调用。")
    public Function<BlogSearchRequest, List<BlogSummaryVO>> searchBlogs() {
        return request -> {
            log.info("Function Calling: searchBlogs with request: {}", request);
            return shopRecommendService.searchBlogs(request);
        };
    }
}
