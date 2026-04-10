package com.yuexiang.blog.service.impl;

import com.yuexiang.blog.constants.LikeMQConstants;
import com.yuexiang.blog.constants.LikeRedisConstants;
import com.yuexiang.blog.domain.entity.Blog;
import com.yuexiang.blog.domain.event.LikeEvent;
import com.yuexiang.blog.mapper.BlogLikeMapper;
import com.yuexiang.blog.mapper.BlogMapper;
import com.yuexiang.blog.mq.producer.LikeEventProducer;
import com.yuexiang.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogLikeServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private BlogLikeMapper blogLikeMapper;

    @Mock
    private LikeEventProducer likeEventProducer;

    @InjectMocks
    private BlogLikeServiceImpl blogLikeService;

    private static final Long USER_ID = 1L;
    private static final Long BLOG_ID = 100L;
    private static final Long AUTHOR_ID = 2L;

    @BeforeEach
    void setUp() {
        DefaultRedisScript<Long> likeScript = new DefaultRedisScript<>();
        ReflectionTestUtils.setField(likeScript, "resultType", Long.class);
        ReflectionTestUtils.setField(blogLikeService, "LIKE_SCRIPT", likeScript);
        ReflectionTestUtils.setField(blogLikeService, "UNLIKE_SCRIPT", likeScript);
        ReflectionTestUtils.setField(blogLikeService, "ROLLBACK_SCRIPT", likeScript);
    }

    @Test
    @DisplayName("点赞成功 - 正常流程")
    void likeBlog_Success() {
        Blog blog = new Blog();
        blog.setId(BLOG_ID);
        blog.setUserId(AUTHOR_ID);
        blog.setDeleted(0);
        when(blogMapper.selectById(BLOG_ID)).thenReturn(blog);

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(1L);

        doNothing().when(blogLikeMapper).insert(any());
        when(blogMapper.updateLikeCount(eq(BLOG_ID), eq(1))).thenReturn(1);

        doNothing().when(redisTemplate).delete(anyString());

        assertDoesNotThrow(() -> blogLikeService.likeBlog(USER_ID, BLOG_ID));

        verify(likeEventProducer).sendLikeEvent(any(LikeEvent.class));
        verify(redisTemplate).delete(LikeRedisConstants.USER_LIKE_COUNT_KEY + AUTHOR_ID);
    }

    @Test
    @DisplayName("点赞失败 - 笔记不存在")
    void likeBlog_BlogNotFound() {
        when(blogMapper.selectById(BLOG_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> blogLikeService.likeBlog(USER_ID, BLOG_ID));

        assertEquals(400, exception.getCode());
        assertEquals("笔记不存在", exception.getMessage());
    }

    @Test
    @DisplayName("点赞失败 - 已点赞")
    void likeBlog_AlreadyLiked() {
        Blog blog = new Blog();
        blog.setId(BLOG_ID);
        blog.setUserId(AUTHOR_ID);
        blog.setDeleted(0);
        when(blogMapper.selectById(BLOG_ID)).thenReturn(blog);

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> blogLikeService.likeBlog(USER_ID, BLOG_ID));

        assertEquals(400, exception.getCode());
        assertEquals("已点赞，请勿重复操作", exception.getMessage());

        verify(likeEventProducer, never()).sendLikeEvent(any());
    }

    @Test
    @DisplayName("取消点赞成功 - 正常流程")
    void unlikeBlog_Success() {
        Blog blog = new Blog();
        blog.setId(BLOG_ID);
        blog.setUserId(AUTHOR_ID);
        blog.setDeleted(0);
        when(blogMapper.selectById(BLOG_ID)).thenReturn(blog);

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(1L);

        when(blogLikeMapper.deleteByUserAndBlog(USER_ID, BLOG_ID)).thenReturn(1);
        when(blogMapper.updateLikeCount(eq(BLOG_ID), eq(-1))).thenReturn(1);

        doNothing().when(redisTemplate).delete(anyString());

        assertDoesNotThrow(() -> blogLikeService.unlikeBlog(USER_ID, BLOG_ID));

        verify(likeEventProducer).sendLikeEvent(argThat(event ->
                event.getDelta().equals(-1) && event.getType().equals(LikeMQConstants.TAG_UNLIKE)
        ));
    }

    @Test
    @DisplayName("取消点赞失败 - 未点赞")
    void unlikeBlog_NotLiked() {
        Blog blog = new Blog();
        blog.setId(BLOG_ID);
        blog.setUserId(AUTHOR_ID);
        blog.setDeleted(0);
        when(blogMapper.selectById(BLOG_ID)).thenReturn(blog);

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> blogLikeService.unlikeBlog(USER_ID, BLOG_ID));

        assertEquals(400, exception.getCode());
        assertEquals("未点赞，无法取消", exception.getMessage());
    }

    @Test
    @DisplayName("获取点赞状态 - Redis缓存命中")
    void isLiked_RedisHit() {
        when(redisTemplate.opsForSet().isMember(anyString(), anyString())).thenReturn(true);

        boolean result = blogLikeService.isLiked(USER_ID, BLOG_ID);

        assertTrue(result);
        verify(blogLikeMapper, never()).countByUserAndBlog(any(), any());
    }

    @Test
    @DisplayName("获取点赞状态 - Redis未命中查询DB")
    void isLiked_RedisMiss_DbHit() {
        when(redisTemplate.opsForSet().isMember(anyString(), anyString())).thenReturn(false);
        when(blogLikeMapper.countByUserAndBlog(USER_ID, BLOG_ID)).thenReturn(1);

        boolean result = blogLikeService.isLiked(USER_ID, BLOG_ID);

        assertTrue(result);
    }

    @Test
    @DisplayName("获取笔记点赞数 - Redis缓存命中")
    void getBlogLikeCount_RedisHit() {
        when(redisTemplate.opsForValue().get(anyString())).thenReturn("10");

        Long count = blogLikeService.getBlogLikeCount(BLOG_ID);

        assertEquals(10L, count);
    }

    @Test
    @DisplayName("获取笔记点赞数 - Redis未命中查询DB")
    void getBlogLikeCount_RedisMiss_DbHit() {
        when(redisTemplate.opsForValue().get(anyString())).thenReturn(null);

        Blog blog = new Blog();
        blog.setLikeCount(5);
        when(blogMapper.selectById(BLOG_ID)).thenReturn(blog);

        Long count = blogLikeService.getBlogLikeCount(BLOG_ID);

        assertEquals(5L, count);
    }
}
