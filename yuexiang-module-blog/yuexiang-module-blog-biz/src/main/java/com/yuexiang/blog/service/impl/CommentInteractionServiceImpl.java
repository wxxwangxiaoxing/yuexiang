package com.yuexiang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexiang.blog.constants.BlogConstants;
import com.yuexiang.blog.domain.entity.BlogComment;
import com.yuexiang.blog.domain.entity.CommentLike;
import com.yuexiang.blog.domain.vo.CommentLikeVO;
import com.yuexiang.blog.domain.vo.ReplyListVO;
import com.yuexiang.blog.mapper.BlogCommentMapper;
import com.yuexiang.blog.mapper.CommentLikeMapper;
import com.yuexiang.blog.service.CommentInteractionService;
import com.yuexiang.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentInteractionServiceImpl implements CommentInteractionService {

    private final BlogCommentMapper blogCommentMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentLikeVO toggleCommentLike(Long commentId, Long userId) {
        BlogComment comment = blogCommentMapper.selectById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new NotFoundException("评论不存在");
        }

        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);
        CommentLike existingLike = commentLikeMapper.selectOne(queryWrapper);

        boolean isLiked;
        if (existingLike == null) {
            CommentLike newLike = new CommentLike();
            newLike.setCommentId(commentId);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            commentLikeMapper.insert(newLike);
            blogCommentMapper.updateLikeCount(commentId, 1);
            isLiked = true;
        } else {
            commentLikeMapper.deleteById(existingLike.getId());
            blogCommentMapper.updateLikeCount(commentId, -1);
            isLiked = false;
        }

        BlogComment updatedComment = blogCommentMapper.selectById(commentId);

        CommentLikeVO vo = new CommentLikeVO();
        vo.setIsLiked(isLiked);
        vo.setLikeCount(updatedComment != null ? updatedComment.getLikeCount() : 0);
        return vo;
    }

    @Override
    public ReplyListVO getReplies(Long commentId, Integer page, Integer size, Long currentUserId) {
        BlogComment rootComment = blogCommentMapper.selectById(commentId);
        if (rootComment == null || rootComment.getDeleted() == 1) {
            throw new NotFoundException("评论不存在");
        }

        Page<BlogComment> pageParam = new Page<>(page, Math.min(size, 50));
        LambdaQueryWrapper<BlogComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogComment::getRootCommentId, commentId)
                .eq(BlogComment::getStatus, BlogConstants.COMMENT_STATUS_NORMAL)
                .eq(BlogComment::getDeleted, 0)
                .orderByDesc(BlogComment::getLikeCount)
                .orderByAsc(BlogComment::getCreateTime);

        Page<BlogComment> result = blogCommentMapper.selectPage(pageParam, wrapper);

        ReplyListVO vo = new ReplyListVO();
        vo.setTotal(result.getTotal());
        vo.setPage(page);
        vo.setSize(size);
        vo.setPages((int) result.getPages());

        if (result.getRecords().isEmpty()) {
            vo.setList(Collections.emptyList());
            return vo;
        }

        Set<Long> likedCommentIds = Collections.emptySet();
        if (currentUserId != null) {
            likedCommentIds = getLikedCommentIds(
                    result.getRecords().stream().map(BlogComment::getId).collect(Collectors.toSet()),
                    currentUserId
            );
        }

        Set<Long> finalLikedCommentIds = likedCommentIds;
        List<ReplyListVO.ReplyVO> replyVOs = result.getRecords().stream()
                .map(reply -> buildReplyVO(reply, finalLikedCommentIds))
                .collect(Collectors.toList());

        vo.setList(replyVOs);
        return vo;
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

    private ReplyListVO.ReplyVO buildReplyVO(BlogComment reply, Set<Long> likedCommentIds) {
        ReplyListVO.ReplyVO vo = new ReplyListVO.ReplyVO();
        vo.setId(reply.getId());
        vo.setContent(reply.getContent());
        vo.setLikeCount(reply.getLikeCount());
        vo.setIsLiked(likedCommentIds.contains(reply.getId()));
        vo.setCreateTime(reply.getCreateTime());

        ReplyListVO.UserVO userVO = new ReplyListVO.UserVO();
        userVO.setId(reply.getUserId());
        userVO.setNickName("用户" + reply.getUserId());
        userVO.setAvatar(null);
        vo.setUser(userVO);

        if (reply.getReplyCommentId() != null && reply.getReplyCommentId() > 0) {
            BlogComment targetComment = blogCommentMapper.selectById(reply.getReplyCommentId());
            if (targetComment != null) {
                ReplyListVO.ReplyToUserVO replyToVO = new ReplyListVO.ReplyToUserVO();
                replyToVO.setId(targetComment.getUserId());
                replyToVO.setNickName("用户" + targetComment.getUserId());
                vo.setReplyTo(replyToVO);
            }
        }

        return vo;
    }
}
