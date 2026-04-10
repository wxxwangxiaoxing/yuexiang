package com.yuexiang.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_browse_history")
@Schema(name = "BrowseHistory", description = "浏览历史实体")
public class BrowseHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "类型(1商户,2笔记)")
    private Integer bizType;

    @Schema(description = "目标ID")
    private Long bizId;

    @Schema(description = "最近浏览时间")
    @TableField("view_time")
    private LocalDateTime viewTime;

    @Schema(description = "累计浏览次数")
    @TableField("view_count")
    private Integer viewCount;
}
