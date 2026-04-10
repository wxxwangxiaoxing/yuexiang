package com.yuexiang.blog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "FavoriteVO", description = "收藏操作响应")
public class FavoriteVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否已收藏")
    private Boolean isFavorited;

    @Schema(description = "收藏数")
    private Integer favoriteCount;
}
