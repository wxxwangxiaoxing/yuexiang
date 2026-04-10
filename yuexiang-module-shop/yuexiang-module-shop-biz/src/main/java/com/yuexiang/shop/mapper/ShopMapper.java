package com.yuexiang.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexiang.shop.domain.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ShopMapper extends BaseMapper<Shop> {

    List<Shop> selectShopListWithDistance(@Param("typeId") Long typeId,
                                          @Param("keyword") String keyword,
                                          @Param("area") String area,
                                          @Param("minPrice") Integer minPrice,
                                          @Param("maxPrice") Integer maxPrice,
                                          @Param("longitude") Double longitude,
                                          @Param("latitude") Double latitude,
                                          @Param("sortBy") String sortBy,
                                          @Param("offset") Integer offset,
                                          @Param("pageSize") Integer pageSize);

    Long countShopList(@Param("typeId") Long typeId,
                       @Param("keyword") String keyword,
                       @Param("area") String area,
                       @Param("minPrice") Integer minPrice,
                       @Param("maxPrice") Integer maxPrice);

    List<String> selectShopTags(@Param("shopId") Long shopId);

    Set<Long> selectFavoriteShopIds(@Param("userId") Long userId,
                                    @Param("shopIds") List<Long> shopIds);

    String selectAiSummary(@Param("shopId") Long shopId);

    List<Shop> selectShopListBasic(@Param("typeId") Long typeId,
                                   @Param("keyword") String keyword,
                                   @Param("area") String area,
                                   @Param("minPrice") Integer minPrice,
                                   @Param("maxPrice") Integer maxPrice,
                                   @Param("sortBy") String sortBy,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    List<Map<String, Object>> selectAreaStats(@Param("typeId") Long typeId);
}
