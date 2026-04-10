package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.domain.entity.Blog;
import com.yuexiang.blog.domain.entity.Favorite;
import com.yuexiang.blog.domain.vo.FavoriteVO;
import com.yuexiang.blog.mapper.BlogMapper;
import com.yuexiang.blog.mapper.BlogFavoriteMapper;
import com.yuexiang.blog.service.FavoriteService;
import com.yuexiang.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service("blogFavoriteService")
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final BlogMapper blogMapper;
    private final BlogFavoriteMapper favoriteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteVO toggleFavorite(Long blogId, Long userId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1 || blog.getStatus() != BlogConstants.BLOG_STATUS_PUBLISHED) {
            throw new NotFoundException("笔记不存在或未发布");
        }

        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getBizType, BlogConstants.BIZ_TYPE_BLOG)
                .eq(Favorite::getBizId, blogId);
        Favorite favorite = favoriteMapper.selectOne(queryWrapper);

        boolean isFavorited;
        if (favorite == null) {
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(userId);
            newFavorite.setBizType(BlogConstants.BIZ_TYPE_BLOG);
            newFavorite.setBizId(blogId);
            newFavorite.setCreateTime(LocalDateTime.now());
            newFavorite.setDeleted(0);
            favoriteMapper.insert(newFavorite);
            blogMapper.updateFavoriteCount(blogId, 1);
            isFavorited = true;
        } else if (favorite.getDeleted() == 1) {
            LambdaUpdateWrapper<Favorite> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Favorite::getId, favorite.getId())
                    .set(Favorite::getDeleted, 0)
                    .set(Favorite::getCreateTime, LocalDateTime.now());
            favoriteMapper.update(null, updateWrapper);
            blogMapper.updateFavoriteCount(blogId, 1);
            isFavorited = true;
        } else {
            LambdaUpdateWrapper<Favorite> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Favorite::getId, favorite.getId())
                    .set(Favorite::getDeleted, 1);
            favoriteMapper.update(null, updateWrapper);
            blogMapper.updateFavoriteCount(blogId, -1);
            isFavorited = false;
        }

        FavoriteVO vo = new FavoriteVO();
        vo.setIsFavorited(isFavorited);
        Integer favoriteCount = blogMapper.getFavoriteCount(blogId);
        vo.setFavoriteCount(favoriteCount != null ? favoriteCount : 0);
        return vo;
    }

    @Override
    public boolean isFavorited(Long blogId, Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getBizType, BlogConstants.BIZ_TYPE_BLOG)
                .eq(Favorite::getBizId, blogId)
                .eq(Favorite::getDeleted, 0);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}
