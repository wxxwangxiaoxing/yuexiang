package com.yuexiang.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_favorite")
@Schema(name = "Favorite", description = "收藏记录实体")
public class Favorite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收藏类型(1商户,2笔记)")
    private Integer bizType;

    @Schema(description = "收藏目标ID")
    private Long bizId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
