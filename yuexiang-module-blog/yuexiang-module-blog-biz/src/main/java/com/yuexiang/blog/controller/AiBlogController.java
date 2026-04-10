package com.yuexiang.blog.controller;

import com.yuexiang.blog.domain.dto.AiExpandDTO;
import com.yuexiang.blog.domain.dto.AiPolishDTO;
import com.yuexiang.blog.domain.dto.AiTagsDTO;
import com.yuexiang.blog.domain.dto.AiTitleDTO;
import com.yuexiang.blog.domain.vo.AiExpandVO;
import com.yuexiang.blog.domain.vo.AiPolishVO;
import com.yuexiang.blog.domain.vo.AiTagsVO;
import com.yuexiang.blog.domain.vo.AiTitleVO;
import com.yuexiang.blog.service.AiBlogService;
import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI笔记辅助", description = "AI辅助创作笔记相关接口")
@RestController
@RequestMapping("/api/ai/blog")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "yuexiang.ai", name = "enabled", havingValue = "true")
public class AiBlogController {

    private final AiBlogService aiBlogService;

    @Operation(summary = "AI生成标题", description = "根据正文内容生成候选标题")
    @PostMapping("/title")
    public CommonResult<AiTitleVO> generateTitle(@Valid @RequestBody AiTitleDTO dto) {
        Long userId = getCurrentUserId();
        AiTitleVO vo = aiBlogService.generateTitle(dto, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "AI润色", description = "润色笔记内容")
    @PostMapping("/polish")
    public CommonResult<AiPolishVO> polishContent(@Valid @RequestBody AiPolishDTO dto) {
        Long userId = getCurrentUserId();
        AiPolishVO vo = aiBlogService.polishContent(dto, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "AI扩写", description = "扩写笔记内容")
    @PostMapping("/expand")
    public CommonResult<AiExpandVO> expandContent(@Valid @RequestBody AiExpandDTO dto) {
        Long userId = getCurrentUserId();
        AiExpandVO vo = aiBlogService.expandContent(dto, userId);
        return CommonResult.success(vo);
    }

    @Operation(summary = "AI打标签", description = "智能推荐标签")
    @PostMapping("/tags")
    public CommonResult<AiTagsVO> suggestTags(@Valid @RequestBody AiTagsDTO dto) {
        Long userId = getCurrentUserId();
        AiTagsVO vo = aiBlogService.suggestTags(dto, userId);
        return CommonResult.success(vo);
    }

    private Long getCurrentUserId() {
        LoginUser user = UserContext.get();
        return user != null ? user.getUserId() : null;
    }
}
