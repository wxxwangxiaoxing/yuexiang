package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "BlogListItemVO", description = "笔记列表卡片项")
public class BlogListItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "笔记ID")
    private Long id;

    @Schema(description = "笔记标题")
    private String title;

    @Schema(description = "封面图（第一张图片）")
    private String cover;

    @Schema(description = "作者昵称")
    private String author;

    @Schema(description = "作者头像")
    private String avatar;

    @Schema(description = "点赞数")
    private Integer likes;
}

