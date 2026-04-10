package com.yuexiang.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_tag")
@Schema(name = "Tag", description = "标签实体")
public class Tag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "类型(0商户标签,1笔记话题)")
    private Integer type;

    @Schema(description = "标签图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "热度(引用次数)")
    private Integer hot;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
