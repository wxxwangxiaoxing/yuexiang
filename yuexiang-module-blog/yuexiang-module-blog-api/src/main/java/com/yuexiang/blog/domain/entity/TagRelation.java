package com.yuexiang.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_tag_relation")
@Schema(name = "TagRelation", description = "标签关联实体")
public class TagRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "标签ID")
    private Long tagId;

    @Schema(description = "业务类型(1商户,2笔记)")
    private Integer bizType;

    @Schema(description = "业务ID")
    private Long bizId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
