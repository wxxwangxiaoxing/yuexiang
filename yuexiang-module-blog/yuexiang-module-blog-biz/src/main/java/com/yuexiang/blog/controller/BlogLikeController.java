package com.yuexiang.blog.controller;

import com.yuexiang.blog.domain.vo.BlogLikeVO;
import com.yuexiang.blog.service.BlogLikeService;
import com.yuexiang.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;

@Tag(name = "笔记点赞", description = "笔记点赞相关接口")
@RestController
@RequestMapping("/api/blog/like")
@RequiredArgsConstructor
public class BlogLikeController {

    private final BlogLikeService blogLikeService;

    @Operation(summary = "点赞笔记", description = "用户对笔记进行点赞操作")
    @PostMapping("/{blogId}")
    public CommonResult<BlogLikeVO> likeBlog(@PathVariable("blogId") Long blogId) {
        Long userId = UserContext.getUserId();
        
        blogLikeService.likeBlog(userId, blogId);
        
        BlogLikeVO vo = new BlogLikeVO();
        vo.setIsLiked(true);
        vo.setLikeCount(blogLikeService.getBlogLikeCount(blogId));
        
        return CommonResult.success(vo);
    }

    @Operation(summary = "取消点赞", description = "用户取消对笔记的点赞")
    @DeleteMapping("/{blogId}")
    public CommonResult<BlogLikeVO> unlikeBlog(@PathVariable("blogId") Long blogId) {
        Long userId = UserContext.getUserId();
        
        blogLikeService.unlikeBlog(userId, blogId);
        
        BlogLikeVO vo = new BlogLikeVO();
        vo.setIsLiked(false);
        vo.setLikeCount(blogLikeService.getBlogLikeCount(blogId));
        
        return CommonResult.success(vo);
    }

    @Operation(summary = "查询点赞状态", description = "查询用户是否已点赞某笔记")
    @GetMapping("/status/{blogId}")
    public CommonResult<BlogLikeVO> getLikeStatus(@PathVariable("blogId") Long blogId) {
        
        BlogLikeVO vo = new BlogLikeVO();
        vo.setLikeCount(blogLikeService.getBlogLikeCount(blogId));
        
        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            vo.setIsLiked(blogLikeService.isLiked(loginUser.getUserId(), blogId));
        } else {
            vo.setIsLiked(false);
        }
        
        return CommonResult.success(vo);
    }
}

