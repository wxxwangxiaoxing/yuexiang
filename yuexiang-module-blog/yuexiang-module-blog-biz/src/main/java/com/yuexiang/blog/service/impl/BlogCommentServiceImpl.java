package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.domain.dto.CommentCreateDTO;
import com.yuexiang.blog.domain.entity.*;
import com.yuexiang.blog.domain.vo.CommentListVO;
import com.yuexiang.blog.domain.vo.CommentCreateVO;
import com.yuexiang.blog.mapper.*;
import com.yuexiang.blog.service.BlogCommentService;
import com.yuexiang.common.exception.BadRequestException;
import com.yuexiang.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogMapper blogMapper;
    private final BlogCommentMapper blogCommentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public CommentListVO getComments(Long blogId, Integer page, Integer size, String sortBy, Long currentUserId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1) {
            throw new NotFoundException("笔记不存在");
        }

        Page<BlogComment> pageParam = new Page<>(page, Math.min(size, 20));
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getBlogId, blogId)
                .eq(BlogComment::getRootCommentId, 0L)
                .eq(BlogComment::getStatus, BlogConstants.COMMENT_STATUS_NORMAL)
                .eq(BlogComment::getDeleted, 0);

        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(BlogComment::getLikeCount)
                    .orderByDesc(BlogComment::getCreateTime);
        } else {
            wrapper.orderByDesc(BlogComment::getCreateTime);
        }

        Page<BlogComment> result = blogCommentMapper.selectPage(pageParam, wrapper);

        CommentListVO vo = new CommentListVO();
        vo.setTotal(result.getTotal());
        vo.setTotalAll((long) blog.getCommentCount());
        vo.setPage(page);
        vo.setSize(size);
        vo.setPages((int) result.getPages());

        if (result.getRecords().isEmpty()) {
            vo.setList(Collections.emptyList());
            return vo;
        }

        List<Long> topCommentIds = result.getRecords().stream()
                .map(BlogComment::getId)
                .collect(Collectors.toList());

        Map<Long, Integer> replyCountMap = getReplyCountMap(topCommentIds);

        Map<Long, List<BlogComment>> repliesMap = getTopReplies(topCommentIds, 3);

        Set<Long> allCommentIds = new HashSet<>(topCommentIds);
        repliesMap.values().forEach(list -> list.forEach(c -> allCommentIds.add(c.getId())));

        final Set<Long> likedCommentIds = currentUserId != null 
                ? getLikedCommentIds(allCommentIds, currentUserId) 
                : Collections.emptySet();

        List<CommentListVO.CommentVO> commentVOs = result.getRecords().stream()
                .map(comment -> buildCommentVO(comment, replyCountMap, repliesMap, likedCommentIds))
                .collect(Collectors.toList());

        vo.setList(commentVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentCreateVO createComment(Long blogId, CommentCreateDTO dto, Long userId) {
        Blog blog = blogMapper.selectById(blogId);
        if (blog == null || blog.getDeleted() == 1 || blog.getStatus() != BlogConstants.BLOG_STATUS_PUBLISHED) {
            throw new NotFoundException("笔记不存在或未发布");
        }

        checkCommentRateLimit(userId);

        Long rootCommentId = dto.getRootCommentId() != null ? dto.getRootCommentId() : 0L;
        Long replyCommentId = dto.getReplyCommentId() != null ? dto.getReplyCommentId() : 0L;

        if (rootCommentId > 0) {
            BlogComment rootComment = blogCommentMapper.selectById(rootCommentId);
            if (rootComment == null || rootComment.getDeleted() == 1) {
                throw new BadRequestException("顶级评论不存在");
            }
            if (!rootComment.getBlogId().equals(blogId)) {
                throw new BadRequestException("评论不属于当前笔记");
            }
        }

        if (replyCommentId > 0) {
            BlogComment replyComment = blogCommentMapper.selectById(replyCommentId);
            if (replyComment == null || replyComment.getDeleted() == 1) {
                throw new BadRequestException("回复的评论不存在");
            }
        }

        BlogComment comment = new BlogComment();
        comment.setUserId(userId);
        comment.setBlogId(blogId);
        comment.setRootCommentId(rootCommentId);
        comment.setReplyCommentId(replyCommentId);
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(BlogConstants.COMMENT_STATUS_NORMAL);
        comment.setDeleted(0);
        comment.setCreateTime(LocalDateTime.now());

        blogCommentMapper.insert(comment);

        blogMapper.updateCommentCount(blogId, 1);

        CommentCreateVO vo = new CommentCreateVO();
        vo.setCommentId(comment.getId());
        vo.setCreateTime(comment.getCreateTime().toString());
        return vo;
    }

    @Override
    public Long getTotalCommentCount(Long blogId) {
        Blog blog = blogMapper.selectById(blogId);
        return blog != null ? (long) blog.getCommentCount() : 0L;
    }

    private void checkCommentRateLimit(Long userId) {
        String key = BlogConstants.COMMENT_RATE_KEY_PREFIX + userId;
        String countStr = stringRedisTemplate.opsForValue().get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= BlogConstants.COMMENT_RATE_LIMIT) {
            throw new BadRequestException("评论过于频繁，请稍后再试");
        }

        if (count == 0) {
            stringRedisTemplate.opsForValue().set(key, "1", BlogConstants.COMMENT_RATE_WINDOW, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().increment(key);
        }
    }

    private Map<Long, Integer> getReplyCountMap(List<Long> topCommentIds) {
        Map<Long, Integer> map = new HashMap<>();
        for (Long id : topCommentIds) {
            LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogComment::getRootCommentId, id)
                    .eq(BlogComment::getStatus, BlogConstants.COMMENT_STATUS_NORMAL)
                    .eq(BlogComment::getDeleted, 0);
            map.put(id, Math.toIntExact(blogCommentMapper.selectCount(wrapper)));
        }
        return map;
    }

    private Map<Long, List<BlogComment>> getTopReplies(List<Long> topCommentIds, int limit) {
        Map<Long, List<BlogComment>> map = new HashMap<>();
        for (Long id : topCommentIds) {
            LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogComment::getRootCommentId, id)
                    .eq(BlogComment::getStatus, BlogConstants.COMMENT_STATUS_NORMAL)
                    .eq(BlogComment::getDeleted, 0)
                    .orderByDesc(BlogComment::getLikeCount)
                    .orderByAsc(BlogComment::getCreateTime)
                    .last("LIMIT " + limit);
            List<BlogComment> replies = blogCommentMapper.selectList(wrapper);
            if (!replies.isEmpty()) {
                map.put(id, replies);
            }
        }
        return map;
    }

    private Set<Long> getLikedCommentIds(Set<Long> commentIds, Long userId) {
        if (commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CommentLike::getCommentId, commentIds)
                .eq(CommentLike::getUserId, userId);
        List<CommentLike> likes = commentLikeMapper.selectList(wrapper);
        return likes.stream().map(CommentLike::getCommentId).collect(Collectors.toSet());
    }

    private CommentListVO.CommentVO buildCommentVO(BlogComment comment,
                                                    Map<Long, Integer> replyCountMap,
                                                    Map<Long, List<BlogComment>> repliesMap,
                                                    Set<Long> likedCommentIds) {
        CommentListVO.CommentVO vo = new CommentListVO.CommentVO();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setIsLiked(likedCommentIds.contains(comment.getId()));
        vo.setCreateTime(comment.getCreateTime());
        vo.setReplyCount(replyCountMap.getOrDefault(comment.getId(), 0));

        CommentListVO.UserVO userVO = new CommentListVO.UserVO();
        userVO.setId(comment.getUserId());
        userVO.setNickName("用户" + comment.getUserId());
        userVO.setAvatar(null);
        vo.setUser(userVO);

        List<BlogComment> replies = repliesMap.getOrDefault(comment.getId(), Collections.emptyList());
        List<CommentListVO.ReplyVO> replyVOs = replies.stream()
                .map(r -> buildReplyVO(r, likedCommentIds))
                .collect(Collectors.toList());
        vo.setReplies(replyVOs);

        return vo;
    }

    private CommentListVO.ReplyVO buildReplyVO(BlogComment reply, Set<Long> likedCommentIds) {
        CommentListVO.ReplyVO vo = new CommentListVO.ReplyVO();
        vo.setId(reply.getId());
        vo.setContent(reply.getContent());
        vo.setLikeCount(reply.getLikeCount());
        vo.setIsLiked(likedCommentIds.contains(reply.getId()));
        vo.setCreateTime(reply.getCreateTime());

        CommentListVO.UserVO userVO = new CommentListVO.UserVO();
        userVO.setId(reply.getUserId());
        userVO.setNickName("用户" + reply.getUserId());
        userVO.setAvatar(null);
        vo.setUser(userVO);

        if (reply.getReplyCommentId() != null && reply.getReplyCommentId() > 0) {
            CommentListVO.ReplyToUserVO replyToVO = new CommentListVO.ReplyToUserVO();
            BlogComment targetComment = blogCommentMapper.selectById(reply.getReplyCommentId());
            if (targetComment != null) {
                replyToVO.setId(targetComment.getUserId());
                replyToVO.setNickName("用户" + targetComment.getUserId());
            }
            vo.setReplyTo(replyToVO);
        }

        return vo;
    }
}
