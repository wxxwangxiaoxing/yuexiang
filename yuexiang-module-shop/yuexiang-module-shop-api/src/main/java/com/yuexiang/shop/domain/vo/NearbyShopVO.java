package com.yuexiang.shop.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 附近商户单条展示信息
 */
@Data
public class NearbyShopVO {

    /**
     * 商户ID
     */
    private Long id;

    /**
     * 商户名称
     */
    private String name;

    /**
     * 类型ID
     */
    private Long typeId;

    /**
     * 商户图片 (JSON格式或首张图)
     */
    private String images;

    /**
     * 所在区域/商圈
     */
    private String area;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 人均价格 (单位：分)
     */
    private Integer avgPrice;

    /**
     * 销量 (对应数据库 sales_count)
     */
    private Integer sold;

    /**
     * 评论数 (对应数据库 comment_count)
     */
    private Integer comments;

    /**
     * 综合评分 (0.0~5.0)
     */
    private BigDecimal score;

    /**
     * 营业时间 (JSON字符串)
     */
    private String openHours;

    /**
     * 距离 (单位：米)
     * 计算逻辑：由 Redis GEO 或计算得出
     */
    private Integer distance;
}