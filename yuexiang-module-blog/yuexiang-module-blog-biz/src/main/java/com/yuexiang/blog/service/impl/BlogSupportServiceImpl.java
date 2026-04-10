package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexiang.blog.domain.entity.Tag;
import com.yuexiang.blog.domain.vo.ShopSearchVO;
import com.yuexiang.blog.domain.vo.TagSuggestVO;
import com.yuexiang.blog.mapper.BlogTagMapper;
import com.yuexiang.blog.service.BlogSupportService;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogSupportServiceImpl implements BlogSupportService {

    private final ShopMapper shopMapper;
    private final BlogTagMapper tagMapper;

    @Override
    public ShopSearchVO searchShop(String keyword, Integer page, Integer size) {
        Page<Shop> pageParam = new Page<>(page, Math.min(size, 20));

        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getDeleted, 0)
                .like(Shop::getName, keyword)
                .orderByDesc(Shop::getScore);

        Page<Shop> result = shopMapper.selectPage(pageParam, wrapper);

        ShopSearchVO vo = new ShopSearchVO();
        vo.setTotal(result.getTotal());
        vo.setPage(page);
        vo.setSize(size);
        vo.setPages((int) result.getPages());

        if (result.getRecords().isEmpty()) {
            vo.setList(Collections.emptyList());
            return vo;
        }

        List<ShopSearchVO.ShopVO> shopVOs = result.getRecords().stream()
                .map(this::convertToShopVO)
                .collect(Collectors.toList());
        vo.setList(shopVOs);

        return vo;
    }

    @Override
    public List<TagSuggestVO> suggestTags(String keyword, Integer size) {
        size = Math.min(size, 20);

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getType, 1)
                .eq(Tag::getDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Tag::getName, keyword);
        }

        wrapper.orderByDesc(Tag::getHot)
                .last("LIMIT " + size);

        List<Tag> tags = tagMapper.selectList(wrapper);

        return tags.stream().map(tag -> {
            TagSuggestVO vo = new TagSuggestVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setHot(tag.getHot());
            return vo;
        }).collect(Collectors.toList());
    }

    private ShopSearchVO.ShopVO convertToShopVO(Shop shop) {
        ShopSearchVO.ShopVO vo = new ShopSearchVO.ShopVO();
        vo.setId(shop.getId());
        vo.setName(shop.getName());
        vo.setAddress(shop.getAddress());
        vo.setAvgPrice(shop.getAvgPrice());
        vo.setScore(shop.getScore() != null ? shop.getScore().toString() : "0.0");
        return vo;
    }
}
