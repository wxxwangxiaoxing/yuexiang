package com.yuexiang.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yuexiang.framework.mybatis.core.dataobject.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商户信息表实体类
 */
@Data
@TableName("tb_shop")
@Schema(name = "Shop", description = "商户信息实体")
public class Shop extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商户名称")
    private String name;

    @Schema(description = "商户类型ID")
    @TableField("type_id")
    private Long typeId;

    @Schema(description = "商户图片(JSON数组)")
    private String images;

    @Schema(description = "商圈区域")
    private String area;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "经度(-180~180)")
    private BigDecimal longitude;

    @Schema(description = "纬度(-90~90)")
    private BigDecimal latitude;

    @Schema(description = "人均价格(分)")
    @TableField("avg_price")
    private Integer avgPrice;

    @Schema(description = "销量")
    @TableField("sales_count")
    private Integer salesCount;

    @Schema(description = "评论数")
    @TableField("comment_count")
    private Integer commentCount;

    @Schema(description = "综合评分(0.0~5.0,贝叶斯加权)")
    private BigDecimal score;

    @Schema(description = "评价总人数")
    @TableField("review_count")
    private Integer reviewCount;

    @Schema(description = "营业时间(JSON格式)")
    @TableField("open_hours")
    private String openHours;

    @TableField(exist = false)
    private BigDecimal distance;

}