package com.yuexiang.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "商户搜索建议")
@RestController
@RequestMapping("/api/shop/search")
@RequiredArgsConstructor
public class ShopSearchController {

    private final ShopMapper shopMapper;
    private final ShopTypeMapper shopTypeMapper;

    @Operation(summary = "搜索建议", description = "输入关键词时返回商户名称建议")
    @GetMapping("/suggest")
    public CommonResult<List<String>> searchSuggest(
            @Parameter(description = "搜索关键词") @RequestParam(value = "keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return CommonResult.success(List.of());
        }

        List<Shop> shops = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>()
                        .like(Shop::getName, keyword.trim())
                        .eq(Shop::getDeleted, 0)
                        .last("LIMIT 10")
        );

        List<String> suggestions = shops.stream()
                .map(Shop::getName)
                .collect(Collectors.toList());

        return CommonResult.success(suggestions);
    }

    @Operation(summary = "热门搜索商户")
    @GetMapping("/hot")
    public CommonResult<List<String>> hotShops(
            @Parameter(description = "数量") @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        List<Shop> shops = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getDeleted, 0)
                        .orderByDesc(Shop::getSalesCount)
                        .last("LIMIT " + limit)
        );

        List<String> names = shops.stream()
                .map(Shop::getName)
                .collect(Collectors.toList());

        return CommonResult.success(names);
    }
}
