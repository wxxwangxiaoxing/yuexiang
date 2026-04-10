package com.yuexiang.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.domain.dto.BlogDraftDTO;
import com.yuexiang.blog.domain.dto.BlogPublishDTO;
import com.yuexiang.blog.domain.entity.Blog;
import com.yuexiang.blog.domain.entity.TagRelation;
import com.yuexiang.blog.domain.vo.BlogDraftVO;
import com.yuexiang.blog.domain.vo.BlogPublishVO;
import com.yuexiang.blog.mapper.BlogMapper;
import com.yuexiang.blog.mapper.TagRelationMapper;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.ForbiddenException;
import com.yuexiang.common.exception.NotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("博客发布服务测试")
class BlogPublishServiceImplTest {

    @Mock
    private BlogMapper blogMapper;
    @Mock
    private TagRelationMapper tagRelationMapper;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BlogPublishServiceImpl blogPublishService;

    private static final Long TEST_USER_ID = 123456L;
    private static final Long TEST_SHOP_ID = 987654L;
    private static final Long TEST_DRAFT_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("全新发布笔记 - 成功")
    void testPublishBlog_NewBlog_Success() throws JsonProcessingException {
        BlogPublishDTO dto = createPublishDTO();
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(objectMapper.writeValueAsString(anyList())).thenReturn("[\"image1.jpg\",\"image2.jpg\"]");
        when(blogMapper.insert(any(Blog.class))).thenAnswer(inv -> {
            Blog blog = inv.getArgument(0);
            blog.setId(100L);
            return 1;
        });

        BlogPublishVO result = blogPublishService.publishBlog(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(100L, result.getBlogId());
        verify(blogMapper, times(1)).insert(any(Blog.class));
        verify(tagRelationMapper, times(2)).insert(any(TagRelation.class));
    }

    @Test
    @DisplayName("草稿转发布 - 成功")
    void testPublishBlog_DraftToPublish_Success() throws JsonProcessingException {
        BlogPublishDTO dto = createPublishDTO();
        dto.setDraftId(TEST_DRAFT_ID);

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(TEST_USER_ID);
        draft.setStatus(BlogConstants.BLOG_STATUS_DRAFT);
        draft.setDeleted(0);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);
        when(objectMapper.writeValueAsString(anyList())).thenReturn("[\"image1.jpg\"]");
        when(blogMapper.update(any(Blog.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        BlogPublishVO result = blogPublishService.publishBlog(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_DRAFT_ID, result.getBlogId());
        verify(blogMapper, times(1)).update(any(Blog.class), any(LambdaUpdateWrapper.class));
        verify(tagRelationMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(tagRelationMapper, times(2)).insert(any(TagRelation.class));
    }

    @Test
    @DisplayName("发布频率限制 - 抛出异常")
    void testPublishBlog_RateLimit_Exception() {
        BlogPublishDTO dto = createPublishDTO();
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blogPublishService.publishBlog(dto, TEST_USER_ID);
        });

        assertEquals("发布过于频繁，请5分钟后再试", exception.getMessage());
    }

    @Test
    @DisplayName("草稿不存在 - 抛出异常")
    void testPublishBlog_DraftNotFound_Exception() {
        BlogPublishDTO dto = createPublishDTO();
        dto.setDraftId(TEST_DRAFT_ID);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            blogPublishService.publishBlog(dto, TEST_USER_ID);
        });

        assertEquals("草稿不存在或已被删除", exception.getMessage());
    }

    @Test
    @DisplayName("草稿权限不足 - 抛出异常")
    void testPublishBlog_DraftPermission_Exception() {
        BlogPublishDTO dto = createPublishDTO();
        dto.setDraftId(TEST_DRAFT_ID);

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(999999L); // 不同用户
        draft.setStatus(BlogConstants.BLOG_STATUS_DRAFT);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            blogPublishService.publishBlog(dto, TEST_USER_ID);
        });

        assertEquals("无权操作该草稿", exception.getMessage());
    }

    @Test
    @DisplayName("草稿状态非草稿 - 抛出异常")
    void testPublishBlog_DraftStatus_Exception() {
        BlogPublishDTO dto = createPublishDTO();
        dto.setDraftId(TEST_DRAFT_ID);

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(TEST_USER_ID);
        draft.setStatus(BlogConstants.BLOG_STATUS_PENDING); // 非草稿状态

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blogPublishService.publishBlog(dto, TEST_USER_ID);
        });

        assertEquals("该笔记当前状态不是草稿，无法执行发布操作", exception.getMessage());
    }

    @Test
    @DisplayName("草稿状态变更 - 抛出异常")
    void testPublishBlog_DraftStateChanged_Exception() {
        BlogPublishDTO dto = createPublishDTO();
        dto.setDraftId(TEST_DRAFT_ID);

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(TEST_USER_ID);
        draft.setStatus(BlogConstants.BLOG_STATUS_DRAFT);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);
        when(blogMapper.update(any(Blog.class), any(LambdaUpdateWrapper.class))).thenReturn(0);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blogPublishService.publishBlog(dto, TEST_USER_ID);
        });

        assertEquals("草稿状态已变更，请刷新后重试", exception.getMessage());
    }

    @Test
    @DisplayName("JSON序列化失败 - 抛出异常")
    void testPublishBlog_JsonSerialization_Exception() throws JsonProcessingException {
        BlogPublishDTO dto = createPublishDTO();
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(objectMapper.writeValueAsString(anyList())).thenThrow(JsonProcessingException.class);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blogPublishService.publishBlog(dto, TEST_USER_ID);
        });

        assertEquals("图片数据处理异常，请重试", exception.getMessage());
    }

    @Test
    @DisplayName("创建草稿 - 成功")
    void testCreateDraft_Success() throws JsonProcessingException {
        BlogDraftDTO dto = createDraftDTO();
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(objectMapper.writeValueAsString(anyList())).thenReturn("[\"image1.jpg\"]");
        when(blogMapper.insert(any(Blog.class))).thenAnswer(inv -> {
            Blog blog = inv.getArgument(0);
            blog.setId(200L);
            return 1;
        });

        BlogDraftVO result = blogPublishService.createDraft(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(200L, result.getBlogId());
        verify(blogMapper, times(1)).insert(any(Blog.class));
        verify(tagRelationMapper, times(2)).insert(any(TagRelation.class));
    }

    @Test
    @DisplayName("草稿保存频率限制 - 抛出异常")
    void testCreateDraft_RateLimit_Exception() {
        BlogDraftDTO dto = createDraftDTO();
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blogPublishService.createDraft(dto, TEST_USER_ID);
        });

        assertEquals("保存过于频繁，请稍后再试", exception.getMessage());
    }

    @Test
    @DisplayName("更新草稿 - 成功")
    void testUpdateDraft_Success() throws JsonProcessingException {
        BlogDraftDTO dto = createDraftDTO();
        dto.setTitle("Updated Title");

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(TEST_USER_ID);
        draft.setStatus(BlogConstants.BLOG_STATUS_DRAFT);
        draft.setDeleted(0);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);
        when(objectMapper.writeValueAsString(anyList())).thenReturn("[\"image1.jpg\"]");
        when(blogMapper.update(any(Blog.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        BlogDraftVO result = blogPublishService.updateDraft(TEST_DRAFT_ID, dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_DRAFT_ID, result.getBlogId());
        verify(blogMapper, times(1)).update(any(Blog.class), any(LambdaUpdateWrapper.class));
        verify(tagRelationMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(tagRelationMapper, times(2)).insert(any(TagRelation.class));
    }

    @Test
    @DisplayName("更新草稿 - 草稿不存在")
    void testUpdateDraft_NotFound_Exception() {
        BlogDraftDTO dto = createDraftDTO();
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            blogPublishService.updateDraft(TEST_DRAFT_ID, dto, TEST_USER_ID);
        });

        assertEquals("草稿不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新草稿 - 权限不足")
    void testUpdateDraft_Permission_Exception() {
        BlogDraftDTO dto = createDraftDTO();

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(999999L); // 不同用户
        draft.setStatus(BlogConstants.BLOG_STATUS_DRAFT);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            blogPublishService.updateDraft(TEST_DRAFT_ID, dto, TEST_USER_ID);
        });

        assertEquals("草稿不属于当前用户", exception.getMessage());
    }

    @Test
    @DisplayName("更新草稿 - 状态非草稿")
    void testUpdateDraft_Status_Exception() {
        BlogDraftDTO dto = createDraftDTO();

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(TEST_USER_ID);
        draft.setStatus(BlogConstants.BLOG_STATUS_PENDING); // 非草稿状态

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blogPublishService.updateDraft(TEST_DRAFT_ID, dto, TEST_USER_ID);
        });

        assertEquals("该笔记不是草稿状态，无法更新", exception.getMessage());
    }

    @Test
    @DisplayName("更新草稿 - 状态变更")
    void testUpdateDraft_StateChanged_Exception() {
        BlogDraftDTO dto = createDraftDTO();

        Blog draft = new Blog();
        draft.setId(TEST_DRAFT_ID);
        draft.setUserId(TEST_USER_ID);
        draft.setStatus(BlogConstants.BLOG_STATUS_DRAFT);

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(blogMapper.selectById(TEST_DRAFT_ID)).thenReturn(draft);
        when(blogMapper.update(any(Blog.class), any(LambdaUpdateWrapper.class))).thenReturn(0);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            blogPublishService.updateDraft(TEST_DRAFT_ID, dto, TEST_USER_ID);
        });

        assertEquals("草稿更新失败，可能已被删除或状态变更", exception.getMessage());
    }

    @Test
    @DisplayName("空标签列表 - 正常处理")
    void testSaveTagRelations_EmptyList() {
        blogPublishService.saveTagRelations(1L, null);
        blogPublishService.saveTagRelations(1L, new ArrayList<>());
        verify(tagRelationMapper, never()).insert(any(TagRelation.class));
    }

    private BlogPublishDTO createPublishDTO() {
        BlogPublishDTO dto = new BlogPublishDTO();
        dto.setShopId(TEST_SHOP_ID);
        dto.setTitle("Test Blog");
        dto.setContent("This is a test blog content");
        dto.setImages(List.of("image1.jpg", "image2.jpg"));
        dto.setLocation("Beijing");
        dto.setTagIds(List.of(1L, 2L));
        return dto;
    }

    private BlogDraftDTO createDraftDTO() {
        BlogDraftDTO dto = new BlogDraftDTO();
        dto.setShopId(TEST_SHOP_ID);
        dto.setTitle("Test Draft");
        dto.setContent("This is a test draft content");
        dto.setImages(List.of("image1.jpg"));
        dto.setLocation("Shanghai");
        dto.setTagIds(List.of(1L, 2L));
        return dto;
    }
}
