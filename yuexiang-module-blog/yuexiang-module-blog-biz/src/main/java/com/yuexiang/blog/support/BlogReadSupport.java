package com.yuexiang.blog.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.domain.entity.BlogLike;
import com.yuexiang.blog.domain.entity.BrowseHistory;
import com.yuexiang.blog.domain.entity.Favorite;
import com.yuexiang.blog.domain.entity.Follow;
import com.yuexiang.blog.domain.entity.TagRelation;
import com.yuexiang.blog.domain.vo.BlogDetailVO;
import com.yuexiang.blog.mapper.BlogFavoriteMapper;
import com.yuexiang.blog.mapper.BlogLikeMapper;
import com.yuexiang.blog.mapper.BlogTagMapper;
import com.yuexiang.blog.mapper.BrowseHistoryMapper;
import com.yuexiang.blog.mapper.FollowMapper;
import com.yuexiang.blog.mapper.TagRelationMapper;
import com.yuexiang.shop.api.ShopReadService;
import com.yuexiang.shop.domain.vo.ShopDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlogReadSupport {

    private final BlogLikeMapper blogLikeMapper;
    private final BlogFavoriteMapper favoriteMapper;
    private final FollowMapper followMapper;
    private final TagRelationMapper tagRelationMapper;
    private final BlogTagMapper tagMapper;
    private final BrowseHistoryMapper browseHistoryMapper;
    private final ObjectMapper objectMapper;
    private final ShopReadService shopReadService;

    public List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析笔记图片JSON失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public BlogDetailVO.UserVO buildUserVO(Long authorId, Long currentUserId) {
        BlogDetailVO.UserVO userVO = new BlogDetailVO.UserVO();
        userVO.setId(authorId);
        userVO.setNickName("用户" + authorId);
        userVO.setAvatar(null);
        userVO.setIsFollowed(currentUserId != null && checkIsFollowed(authorId, currentUserId));
        return userVO;
    }

    public BlogDetailVO.ShopVO buildShopVO(Long shopId) {
        if (shopId == null) {
            return null;
        }
        try {
            ShopDetailVO shopDetail = shopReadService.getShopDetail(shopId, null);
            if (shopDetail == null) {
                return null;
            }
            BlogDetailVO.ShopVO shopVO = new BlogDetailVO.ShopVO();
            shopVO.setId(shopDetail.getId());
            shopVO.setName(shopDetail.getName());
            shopVO.setAddress(shopDetail.getAddress());
            shopVO.setAvgPrice(shopDetail.getAvgPrice() != null ? Integer.parseInt(shopDetail.getAvgPrice()) : null);
            return shopVO;
        } catch (Exception e) {
            log.warn("获取关联商户信息失败: shopId={}", shopId, e);
            return null;
        }
    }

    public List<BlogDetailVO.TagVO> buildTagVOs(Long blogId) {
        LambdaQueryWrapper<TagRelation> trWrapper = new LambdaQueryWrapper<>();
        trWrapper.eq(TagRelation::getBizType, BlogConstants.BIZ_TYPE_BLOG)
                .eq(TagRelation::getBizId, blogId);

        List<Long> tagIds = tagRelationMapper.selectList(trWrapper).stream()
                .map(TagRelation::getTagId)
                .toList();
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }

        return tagMapper.selectBatchIds(tagIds).stream().map(tag -> {
            BlogDetailVO.TagVO tagVO = new BlogDetailVO.TagVO();
            tagVO.setId(tag.getId());
            tagVO.setName(tag.getName());
            return tagVO;
        }).toList();
    }

    public boolean checkIsLiked(Long blogId, Long userId) {
        LambdaQueryWrapper<BlogLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogLike::getBlogId, blogId)
                .eq(BlogLike::getUserId, userId);
        return blogLikeMapper.selectCount(wrapper) > 0;
    }

    public boolean checkIsFavorited(Long blogId, Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getBizType, BlogConstants.BIZ_TYPE_BLOG)
                .eq(Favorite::getBizId, blogId)
                .eq(Favorite::getDeleted, 0);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Async
    public void asyncRecordBrowseHistory(Long blogId, Long userId) {
        try {
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setBizType(BlogConstants.BIZ_TYPE_BLOG);
            history.setBizId(blogId);
            browseHistoryMapper.insertOrUpdate(history);
        } catch (Exception e) {
            log.warn("记录浏览历史失败: {}", e.getMessage());
        }
    }

    private boolean checkIsFollowed(Long authorId, Long currentUserId) {
        if (authorId.equals(currentUserId)) {
            return false;
        }
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, currentUserId)
                .eq(Follow::getFollowUserId, authorId)
                .eq(Follow::getDeleted, 0);
        return followMapper.selectCount(wrapper) > 0;
    }
}
