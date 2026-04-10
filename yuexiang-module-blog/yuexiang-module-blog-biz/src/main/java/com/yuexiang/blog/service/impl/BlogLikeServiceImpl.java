package com.yuexiang.blog.service.impl;

import com.yuexiang.blog.constants.LikeMQConstants;
import com.yuexiang.blog.constants.LikeRedisConstants;
import com.yuexiang.blog.domain.entity.Blog;
import com.yuexiang.blog.domain.entity.BlogLike;
import com.yuexiang.blog.domain.event.LikeEvent;
import com.yuexiang.blog.mapper.BlogLikeMapper;
import com.yuexiang.blog.mapper.BlogMapper;
import com.yuexiang.blog.mq.producer.LikeEventProducer;
import com.yuexiang.blog.service.BlogLikeService;
import com.yuexiang.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogLikeServiceImpl implements BlogLikeService {

    private final StringRedisTemplate redisTemplate;
    private final BlogMapper blogMapper;
    private final BlogLikeMapper blogLikeMapper;

    @Autowired(required = false)
    private LikeEventProducer likeEventProducer;

    private static final DefaultRedisScript<Long> LIKE_SCRIPT;
    private static final DefaultRedisScript<Long> UNLIKE_SCRIPT;
    private static final DefaultRedisScript<Long> ROLLBACK_SCRIPT;

    static {
        LIKE_SCRIPT = new DefaultRedisScript<>();
        LIKE_SCRIPT.setLocation(new ClassPathResource("lua/like.lua"));
        LIKE_SCRIPT.setResultType(Long.class);

        UNLIKE_SCRIPT = new DefaultRedisScript<>();
        UNLIKE_SCRIPT.setLocation(new ClassPathResource("lua/unlike.lua"));
        UNLIKE_SCRIPT.setResultType(Long.class);

        ROLLBACK_SCRIPT = new DefaultRedisScript<>();
        ROLLBACK_SCRIPT.setLocation(new ClassPathResource("lua/rollback_like.lua"));
        ROLLBACK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public void likeBlog(Long userId, Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new BusinessException(400, "笔记不存在");
        }
        Long authorId = blog.getUserId();

        List<String> keys = List.of(
                LikeRedisConstants.BLOG_LIKED_KEY + blogId,
                LikeRedisConstants.BLOG_LIKE_COUNT_KEY + blogId
        );
        Long result = redisTemplate.execute(LIKE_SCRIPT, keys, String.valueOf(userId));

        if (result == null || result == 0L) {
            throw new BusinessException(400, "已点赞，请勿重复操作");
        }

        try {
            persistLike(userId, blogId);
        } catch (Exception e) {
            rollbackRedis(userId, blogId, LikeMQConstants.TAG_LIKE);
            log.error("[Like] DB写入失败，已回滚Redis. userId={}, blogId={}", userId, blogId, e);
            throw new BusinessException(500, "点赞失败，请重试");
        }
        LikeEvent event = LikeEvent.builder()
                .authorId(authorId)
                .blogId(blogId)
                .likeUserId(userId)
                .delta(1)
                .type(LikeMQConstants.TAG_LIKE)
                .timestamp(System.currentTimeMillis())
                .build();
        if (likeEventProducer != null) {
            likeEventProducer.sendLikeEvent(event);
        }
        redisTemplate.delete(LikeRedisConstants.USER_LIKE_COUNT_KEY + authorId);
        log.info("[Like] 点赞成功. userId={}, blogId={}, authorId={}", userId, blogId, authorId);
    }

    @Override
    public void unlikeBlog(Long userId, Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new BusinessException(400, "笔记不存在");
        }
        Long authorId = blog.getUserId();

        List<String> keys = List.of(
                LikeRedisConstants.BLOG_LIKED_KEY + blogId,
                LikeRedisConstants.BLOG_LIKE_COUNT_KEY + blogId
        );
        Long result = redisTemplate.execute(UNLIKE_SCRIPT, keys, String.valueOf(userId));

        if (result == null || result == 0L) {
            throw new BusinessException(400, "未点赞，无法取消");
        }

        try {
            persistUnlike(userId, blogId);
        } catch (Exception e) {
            rollbackRedis(userId, blogId, LikeMQConstants.TAG_UNLIKE);
            log.error("[Unlike] DB写入失败，已回滚Redis. userId={}, blogId={}", userId, blogId, e);
            throw new BusinessException(500, "取消点赞失败，请重试");
        }

        LikeEvent event = LikeEvent.builder()
                .authorId(authorId)
                .blogId(blogId)
                .likeUserId(userId)
                .delta(-1)
                .type(LikeMQConstants.TAG_UNLIKE)
                .timestamp(System.currentTimeMillis())
                .build();
        if (likeEventProducer != null) {
            likeEventProducer.sendLikeEvent(event);
        }

        redisTemplate.delete(LikeRedisConstants.USER_LIKE_COUNT_KEY + authorId);

        log.info("[Unlike] 取消点赞成功. userId={}, blogId={}, authorId={}", userId, blogId, authorId);
    }

    @Override
    public boolean isLiked(Long userId, Long blogId) {
        Boolean isMember = redisTemplate.opsForSet()
                .isMember(LikeRedisConstants.BLOG_LIKED_KEY + blogId, String.valueOf(userId));
        if (Boolean.TRUE.equals(isMember)) {
            return true;
        }
        return blogLikeMapper.countByUserAndBlog(userId, blogId) > 0;
    }

    @Override
    public Long getBlogLikeCount(Long blogId) {
        String countStr = redisTemplate.opsForValue().get(LikeRedisConstants.BLOG_LIKE_COUNT_KEY + blogId);
        if (countStr != null) {
            return Long.parseLong(countStr);
        }
        Blog blog = blogMapper.selectById(blogId);
        return blog != null ? blog.getLikeCount().longValue() : 0L;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void persistLike(Long userId, Long blogId) {
        BlogLike blogLike = new BlogLike();
        blogLike.setUserId(userId);
        blogLike.setBlogId(blogId);
        blogLikeMapper.insert(blogLike);
        blogMapper.updateLikeCount(blogId, 1);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void persistUnlike(Long userId, Long blogId) {
        blogLikeMapper.deleteByUserAndBlog(userId, blogId);
        blogMapper.updateLikeCount(blogId, -1);
    }

    private void rollbackRedis(Long userId, Long blogId, String type) {
        try {
            List<String> keys = List.of(
                    LikeRedisConstants.BLOG_LIKED_KEY + blogId,
                    LikeRedisConstants.BLOG_LIKE_COUNT_KEY + blogId
            );
            redisTemplate.execute(ROLLBACK_SCRIPT, keys, String.valueOf(userId), type);
        } catch (Exception rollbackEx) {
            log.error("[Like] Redis回滚也失败! userId={}, blogId={}, type={}", userId, blogId, type, rollbackEx);
        }
    }
}
