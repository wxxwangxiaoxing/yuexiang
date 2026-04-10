package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.constants.BlogPublishConstants;
import com.yuexiang.blog.domain.dto.BlogDraftDTO;
import com.yuexiang.blog.domain.dto.BlogPublishDTO;
import com.yuexiang.blog.domain.entity.Blog;
import com.yuexiang.blog.domain.entity.TagRelation;
import com.yuexiang.blog.domain.vo.BlogDraftVO;
import com.yuexiang.blog.domain.vo.BlogPublishVO;
import com.yuexiang.blog.mapper.BlogMapper;
import com.yuexiang.blog.mapper.TagRelationMapper;
import com.yuexiang.blog.service.BlogPublishService;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.ForbiddenException;
import com.yuexiang.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogPublishServiceImpl implements BlogPublishService {

    private final BlogMapper blogMapper;
    private final TagRelationMapper tagRelationMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BlogPublishVO publishBlog(BlogPublishDTO dto, Long userId) {
        // 1. 检查发布频率限制
        checkPublishRateLimit(userId);

        Long draftId = dto.getDraftId();
        Blog blog;

        // 2. 处理草稿转发布逻辑
        if (draftId != null && draftId > 0) {
            blog = blogMapper.selectById(draftId);
            if (blog == null || blog.getDeleted() == 1) {
                throw new NotFoundException("草稿不存在或已被删除");
            }
            // 权限校验
            if (!blog.getUserId().equals(userId)) {
                throw new ForbiddenException("无权操作该草稿");
            }
            // 状态校验：必须是草稿状态才能转为发布
            if (blog.getStatus() != BlogConstants.BLOG_STATUS_DRAFT) {
                throw new BadRequestException("该笔记当前状态不是草稿，无法执行发布操作");
            }

            // 更新内容
            updateBlogFromPublishDTO(blog, dto);
            blog.setStatus(BlogConstants.BLOG_STATUS_PENDING);
            blog.setUpdateTime(LocalDateTime.now());

            // 使用 Wrapper 确保更新的是属于当前用户的草稿，防止并发修改
            LambdaUpdateWrapper<Blog> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Blog::getId, draftId)
                    .eq(Blog::getUserId, userId)
                    .eq(Blog::getStatus, BlogConstants.BLOG_STATUS_DRAFT)
                    .eq(Blog::getDeleted, 0);

            int updateSuccess = blogMapper.update(blog, updateWrapper);
            if (updateSuccess == 0) {
                // 如果更新行数为0，说明数据状态已被改变（如已被删除或已发布）
                throw new BadRequestException("草稿状态已变更，请刷新后重试");
            }

            // 删除旧的标签关联
            deleteTagRelations(draftId);
        } else {
            // 3. 全新发布逻辑
            blog = new Blog();
            blog.setShopId(dto.getShopId());
            blog.setUserId(userId);
            blog.setTitle(dto.getTitle());
            blog.setContent(dto.getContent());
            blog.setImages(toJsonString(dto.getImages()));
            blog.setLocation(dto.getLocation());
            blog.setLikeCount(0);
            blog.setCommentCount(0);
            blog.setFavoriteCount(0);
            blog.setStatus(BlogConstants.BLOG_STATUS_PENDING);
            blog.setCreateTime(LocalDateTime.now());
            blog.setUpdateTime(LocalDateTime.now());
            blog.setDeleted(0);
            
            blogMapper.insert(blog);
        }

        // 4. 保存标签关联
        saveTagRelations(blog.getId(), dto.getTagIds());

        BlogPublishVO vo = new BlogPublishVO();
        vo.setBlogId(blog.getId());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BlogDraftVO createDraft(BlogDraftDTO dto, Long userId) {
        // 检查保存频率
        checkDraftRateLimit(userId);

        Blog blog = new Blog();
        blog.setShopId(dto.getShopId());
        blog.setUserId(userId);
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        blog.setImages(toJsonString(dto.getImages()));
        blog.setLocation(dto.getLocation());
        blog.setLikeCount(0);
        blog.setCommentCount(0);
        blog.setFavoriteCount(0);
        blog.setStatus(BlogConstants.BLOG_STATUS_DRAFT);
        blog.setCreateTime(LocalDateTime.now());
        blog.setUpdateTime(LocalDateTime.now());
        blog.setDeleted(0);
        
        blogMapper.insert(blog);

        saveTagRelations(blog.getId(), dto.getTagIds());

        BlogDraftVO vo = new BlogDraftVO();
        vo.setBlogId(blog.getId());
        vo.setSavedTime(blog.getCreateTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BlogDraftVO updateDraft(Long draftId, BlogDraftDTO dto, Long userId) {
        // 检查保存频率
        checkDraftRateLimit(userId);

        Blog blog = blogMapper.selectById(draftId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new NotFoundException("草稿不存在");
        }
        if (!blog.getUserId().equals(userId)) {
            throw new ForbiddenException("草稿不属于当前用户");
        }
        if (blog.getStatus() != BlogConstants.BLOG_STATUS_DRAFT) {
            throw new BadRequestException("该笔记不是草稿状态，无法更新");
        }

        blog.setShopId(dto.getShopId());
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        blog.setImages(toJsonString(dto.getImages()));
        blog.setLocation(dto.getLocation());
        blog.setUpdateTime(LocalDateTime.now());

        LambdaUpdateWrapper<Blog> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Blog::getId, draftId)
                .eq(Blog::getUserId, userId)
                .eq(Blog::getStatus, BlogConstants.BLOG_STATUS_DRAFT)
                .eq(Blog::getDeleted, 0);
        
        int updateSuccess = blogMapper.update(blog, updateWrapper);
        if (updateSuccess == 0) {
            throw new BadRequestException("草稿更新失败，可能已被删除或状态变更");
        }

        // 重新关联标签：先删后增
        deleteTagRelations(draftId);
        saveTagRelations(draftId, dto.getTagIds());

        BlogDraftVO vo = new BlogDraftVO();
        vo.setBlogId(draftId);
        vo.setSavedTime(blog.getUpdateTime());
        return vo;
    }

    /**
     * 检查发布限流
     * 优化：使用 setIfAbsent 保证原子性，防止高并发下失效
     */
    private void checkPublishRateLimit(Long userId) {
        String key = BlogPublishConstants.BLOG_RATE_KEY_PREFIX + userId;
        // setIfAbsent 对应 Redis 的 SETNX 命令
        // 如果返回 false，说明 Key 已存在，即还在冷却期内
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(
                key, "1", BlogPublishConstants.BLOG_RATE_WINDOW_SECONDS, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(success)) {
            throw new BadRequestException("发布过于频繁，请5分钟后再试");
        }
    }

    /**
     * 检查草稿保存限流
     */
    private void checkDraftRateLimit(Long userId) {
        String key = BlogPublishConstants.DRAFT_RATE_KEY_PREFIX + userId;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(
                key, "1", BlogPublishConstants.DRAFT_RATE_WINDOW_SECONDS, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(success)) {
            throw new BadRequestException("保存过于频繁，请稍后再试");
        }
    }

    private void updateBlogFromPublishDTO(Blog blog, BlogPublishDTO dto) {
        blog.setShopId(dto.getShopId());
        blog.setTitle(dto.getTitle());
        blog.setContent(dto.getContent());
        blog.setImages(toJsonString(dto.getImages()));
        blog.setLocation(dto.getLocation());
    }

    /**
     * 优化：JSON 序列化失败时抛出异常，避免数据不一致
     */
    private String toJsonString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("图片列表序列化失败: {}", list, e);
            // 抛出业务异常，阻断流程，防止存入 null
            throw new BadRequestException("图片数据处理异常，请重试");
        }
    }

    private void deleteTagRelations(Long blogId) {
        LambdaQueryWrapper<TagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagRelation::getBizType, BlogConstants.BIZ_TYPE_BLOG)
                .eq(TagRelation::getBizId, blogId);
        tagRelationMapper.delete(wrapper);
    }

    private void saveTagRelations(Long blogId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        for (Long tagId : tagIds) {
            TagRelation relation = new TagRelation();
            relation.setTagId(tagId);
            relation.setBizType(BlogConstants.BIZ_TYPE_BLOG);
            relation.setBizId(blogId);
            relation.setCreateTime(LocalDateTime.now());
            tagRelationMapper.insert(relation);
            
            // 注意：此处若需更新标签热度，建议异步处理，避免拖慢主流程
        }
    }
}
