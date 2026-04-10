package com.yuexiang.blog.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "BlogDraftDTO", description = "草稿保存请求")
public class BlogDraftDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 255, message = "标题不能超过255字符")
    @Schema(description = "笔记标题")
    private String title;

    @Size(max = 10000, message = "正文不能超过10000字符")
    @Schema(description = "笔记正文")
    private String content;

    @Size(max = 9, message = "图片数量不能超过9张")
    @Schema(description = "笔记图片路径列表")
    private List<String> images;

    @Schema(description = "关联商户ID")
    private Long shopId;

    @Size(max = 10, message = "标签数量不能超过10个")
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Size(max = 128, message = "地点不能超过128字符")
    @Schema(description = "发布地点")
    private String location;
}
