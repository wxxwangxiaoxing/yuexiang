package com.yuexiang.blog.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "BlogPublishDTO", description = "发布笔记请求")
public class BlogPublishDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "草稿ID(从草稿发布时传入)")
    private Long draftId;

    @Schema(description = "关联商户ID", required = true)
    private Long shopId;

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 255, message = "标题长度需在1-255字符之间")
    @Schema(description = "笔记标题", required = true)
    private String title;

    @Size(max = 10000, message = "正文不能超过10000字符")
    @Schema(description = "笔记正文")
    private String content;

    @Size(min = 1, max = 9, message = "图片数量需在1-9张之间")
    @Schema(description = "笔记图片路径列表", required = true)
    private List<String> images;

    @Size(max = 10, message = "标签数量不能超过10个")
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Size(max = 128, message = "地点不能超过128字符")
    @Schema(description = "发布地点")
    private String location;
}
