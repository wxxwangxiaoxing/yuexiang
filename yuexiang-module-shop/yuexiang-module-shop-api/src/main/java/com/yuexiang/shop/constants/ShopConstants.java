package com.yuexiang.shop.constants;

public class ShopConstants {

    /**
     * Redis GEO 存储的 Key 前缀
     * 完整 Key 示例: "shop:geo:1" (1 为 typeId)
     */
    public static final String SHOP_GEO_KEY = "shop:geo:";

    /**
     * 商户基本信息缓存前缀 (通常用于从 GEO 获取 ID 后，快速查询商户详情)
     */
    public static final String SHOP_CACHE_KEY = "shop:info:";

    /**
     * 商户缓存过期时间（单位：秒）- 默认 30 分钟
     */
    public static final Long SHOP_CACHE_TTL = 1800L;


    // --- 业务逻辑配置 ---

    /**
     * 附近查询的默认半径（单位：公里）
     * 对应代码中的 GEO_RADIUS_KM
     */
    public static final double GEO_RADIUS_KM = 5.0;

    /**
     * 允许查询的最大页码
     * 对应代码中的 MAX_PAGE，防止深度分页攻击和性能抖动
     */
    public static final int MAX_PAGE = 20;

    /**
     * 默认每页显示的条数
     */
    public static final int DEFAULT_PAGE_SIZE = 10;


    // --- 数据库兜底相关配置 ---

    /**
     * 数据库查询时的最大距离限制（单位：米）
     * 1公里 = 1000米，用于 SQL 中的范围计算
     */
    public static final int DB_SEARCH_RADIUS_METERS = 5000;

    /**
     * 异常重试次数或信号量配置（可选）
     */
    public static final int REDIS_RETRY_COUNT = 3;
}