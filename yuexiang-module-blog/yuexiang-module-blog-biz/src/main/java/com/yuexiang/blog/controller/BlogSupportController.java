package com.yuexiang.blog.controller;

import com.yuexiang.blog.domain.vo.ShopSearchVO;
import com.yuexiang.blog.domain.vo.TagSuggestVO;
import com.yuexiang.blog.service.BlogSupportService;
import com.yuexiang.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "笔记辅助", description = "发布笔记辅助接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BlogSupportController {

    private final BlogSupportService blogSupportService;

    @Operation(summary = "搜索商户", description = "发布笔记时搜索关联商户")
    @GetMapping("/blog/shop/search")
    public CommonResult<ShopSearchVO> searchShop(
            @Parameter(description = "搜索关键词") @RequestParam(value = "keyword") String keyword,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "size", defaultValue = "10") Integer size) {
        ShopSearchVO vo = blogSupportService.searchShop(keyword, page, size);
        return CommonResult.success(vo);
    }

    @Operation(summary = "标签推荐", description = "搜索或推荐标签")
    @GetMapping("/tag/suggest")
    public CommonResult<List<TagSuggestVO>> suggestTags(
            @Parameter(description = "搜索关键词(空=热门推荐)") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "返回条数") @RequestParam(value = "size", defaultValue = "10") Integer size) {
        List<TagSuggestVO> list = blogSupportService.suggestTags(keyword, size);
        return CommonResult.success(list);
    }
}
