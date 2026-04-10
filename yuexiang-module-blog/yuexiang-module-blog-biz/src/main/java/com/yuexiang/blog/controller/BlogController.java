package com.yuexiang.blog.controller;

import com.yuexiang.blog.domain.dto.CommentCreateDTO;
import com.yuexiang.blog.domain.vo.*;
import com.yuexiang.blog.service.*;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "笔记详情", description = "笔记详情页相关接口")
@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogCommentService blogCommentService;
    private final FavoriteService favoriteService;
    private final AiSummaryService aiSummaryService;
    private final CommentInteractionService commentInteractionService;

    @Operation(summary = "笔记详情", description = "获取笔记详情信息")
    @GetMapping("/{id}")
    public CommonResult<BlogDetailVO> getBlogDetail(
            @Parameter(description = "笔记ID") @PathVariable("id") Long id) {
        Long currentUserId = getCurrentUserId();
        BlogDetailVO vo = blogService.getBlogDetail(id, currentUserId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "笔记列表", description = "获取笔记列表（首页探店瀑布流）")
    @GetMapping("/list")
    public CommonResult<PageResult<BlogListItemVO>> getBlogList(
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页条数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long currentUserId = getCurrentUserId();
        PageResult<BlogListItemVO> vo = blogService.getBlogList(currentUserId, pageNo, pageSize);
        return CommonResult.success(vo);
    }

    @Operation(summary = "AI摘要", description = "获取笔记AI摘要（懒加载）")
    @GetMapping("/{id}/ai-summary")
    public CommonResult<AiSummaryVO> getAiSummary(
            @Parameter(description = "笔记ID") @PathVariable("id") Long id) {
        AiSummaryVO vo = aiSummaryService.getSummary(id);
        return CommonResult.success(vo);
    }

    @Operation(summary = "评论列表", description = "获取笔记评论列表（顶级分页+子评论预加载）")
    @GetMapping("/{id}/comments")
    public CommonResult<CommentListVO> getComments(
            @Parameter(description = "笔记ID") @PathVariable("id") Long id,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "排序方式(hot/new)") @RequestParam(value = "sortBy", defaultValue = "hot") String sortBy) {
        Long currentUserId = getCurrentUserId();
        CommentListVO vo = blogCommentService.getComments(id, page, size, sortBy, currentUserId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "发表评论", description = "发表笔记评论或回复")
    @PostMapping("/{id}/comments")
    public CommonResult<CommentCreateVO> createComment(
            @Parameter(description = "笔记ID") @PathVariable("id") Long id,
            @Valid @RequestBody CommentCreateDTO dto) {
        Long userId = getRequiredUserId();
        CommentCreateVO vo = blogCommentService.createComment(id, dto, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "收藏/取消收藏", description = "切换笔记收藏状态")
    @PutMapping("/{id}/favorite")
    public CommonResult<FavoriteVO> toggleFavorite(
            @Parameter(description = "笔记ID") @PathVariable("id") Long id) {
        Long userId = getRequiredUserId();
        FavoriteVO vo = favoriteService.toggleFavorite(id, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "评论点赞", description = "切换评论点赞状态")
    @PutMapping("/comments/{commentId}/like")
    public CommonResult<CommentLikeVO> toggleCommentLike(
            @Parameter(description = "评论ID") @PathVariable("commentId") Long commentId) {
        Long userId = getRequiredUserId();
        CommentLikeVO vo = commentInteractionService.toggleCommentLike(commentId, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "展开子评论", description = "获取某条顶级评论的全部子评论")
    @GetMapping("/comments/{commentId}/replies")
    public CommonResult<ReplyListVO> getReplies(
            @Parameter(description = "顶级评论ID") @PathVariable("commentId") Long commentId,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long currentUserId = getCurrentUserId();
        ReplyListVO vo = commentInteractionService.getReplies(commentId, page, size, currentUserId);
        return CommonResult.success(vo);
    }

    private Long getCurrentUserId() {
        LoginUser user = UserContext.get();
        return user != null ? user.getUserId() : null;
    }

    private Long getRequiredUserId() {
        return UserContext.getUserId();
    }
}

