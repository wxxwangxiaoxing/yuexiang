package com.yuexiang.blog.controller;

import com.yuexiang.blog.domain.dto.BlogDraftDTO;
import com.yuexiang.blog.domain.dto.BlogPublishDTO;
import com.yuexiang.blog.domain.vo.BlogDraftVO;
import com.yuexiang.blog.domain.vo.BlogPublishVO;
import com.yuexiang.blog.service.BlogPublishService;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "发布笔记", description = "发布笔记相关接口")
@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogPublishController {

    private final BlogPublishService blogPublishService;

    @Operation(summary = "发布笔记", description = "发布新笔记或从草稿发布")
    @PostMapping
    public CommonResult<BlogPublishVO> publishBlog(@Valid @RequestBody BlogPublishDTO dto) {
        Long userId = UserContext.getUserId();
        BlogPublishVO vo = blogPublishService.publishBlog(dto, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "创建草稿", description = "保存笔记草稿")
    @PostMapping("/draft")
    public CommonResult<BlogDraftVO> createDraft(@RequestBody BlogDraftDTO dto) {
        Long userId = UserContext.getUserId();
        BlogDraftVO vo = blogPublishService.createDraft(dto, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "更新草稿", description = "更新已有草稿内容")
    @PutMapping("/draft/{id}")
    public CommonResult<BlogDraftVO> updateDraft(
            @Parameter(description = "草稿ID") @PathVariable("id") Long id,
            @RequestBody BlogDraftDTO dto) {
        Long userId = UserContext.getUserId();
        BlogDraftVO vo = blogPublishService.updateDraft(id, dto, userId);
        return CommonResult.success(vo);
    }
}

