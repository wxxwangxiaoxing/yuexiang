package com.yuexiang.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 附近商户滚动加载结果包装类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyShopPageVO {

    /**
     * 商户列表数据
     */
    private List<NearbyShopVO> records;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;

    /**
     * 本页最后一条的距离（下页游标）
     */
    private Double lastDistance;
}