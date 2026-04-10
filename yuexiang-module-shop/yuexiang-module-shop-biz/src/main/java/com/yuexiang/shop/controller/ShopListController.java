package com.yuexiang.shop.controller;

import com.yuexiang.common.pojo.CommonResult;
import com.yuexiang.common.pojo.PageResult;
import com.yuexiang.shop.domain.dto.ShopListQueryDTO;
import com.yuexiang.shop.domain.vo.ShopAreaVO;
import com.yuexiang.shop.domain.vo.ShopListPageVO;
import com.yuexiang.shop.domain.vo.ShopListVO;
import com.yuexiang.shop.service.ShopAreaService;
import com.yuexiang.shop.service.ShopEsSyncService;
import com.yuexiang.shop.service.ShopListService;
import com.yuexiang.shop.service.ShopSearchService;
import com.yuexiang.framework.security.core.LoginUser;
import com.yuexiang.framework.security.core.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "商户列表", description = "商户列表相关接口")
@RestController
@RequestMapping("/api/shop")
public class ShopListController {

    private final ShopListService shopListService;
    private final ShopAreaService shopAreaService;

    @Autowired(required = false)
    private ShopSearchService shopSearchService;

    @Autowired(required = false)
    private ShopEsSyncService shopEsSyncService;

    public ShopListController(ShopListService shopListService, ShopAreaService shopAreaService) {
        this.shopListService = shopListService;
        this.shopAreaService = shopAreaService;
    }

    @Operation(summary = "商户列表查询", description = "支持多条件筛选、多种排序方式的商户列表查询")
    @GetMapping("/list")
    public CommonResult<ShopListPageVO> queryShopList(
            @Parameter(description = "商户类型ID") @RequestParam(value = "typeId", required = false) Long typeId,
            @Parameter(description = "搜索关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "商圈区域") @RequestParam(value = "area", required = false) String area,
            @Parameter(description = "排序方式: distance/score/price_asc/price_desc/ai") 
            @RequestParam(value = "sortBy", defaultValue = "score") String sortBy,
            @Parameter(description = "用户经度（sortBy=distance时必传）") @RequestParam(value = "longitude", required = false) Double longitude,
            @Parameter(description = "用户纬度（sortBy=distance时必传）") @RequestParam(value = "latitude", required = false) Double latitude,
            @Parameter(description = "最低人均（元）") @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @Parameter(description = "最高人均（元）") @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页条数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        ShopListQueryDTO query = new ShopListQueryDTO();
        query.setTypeId(typeId);
        query.setKeyword(keyword);
        query.setArea(area);
        query.setSortBy(sortBy);
        query.setLongitude(longitude);
        query.setLatitude(latitude);
        query.setMinPrice(minPrice);
        query.setMaxPrice(maxPrice);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        
        Long userId = null;
        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            userId = loginUser.getUserId();
        }
        
        return CommonResult.success(shopListService.queryShopList(query, userId));
    }

    @Operation(summary = "商圈区域列表", description = "获取商圈区域列表及其商户数量")
    @GetMapping("/area/list")
    public CommonResult<List<ShopAreaVO>> queryAreaList(
            @Parameter(description = "商户类型ID") @RequestParam(value = "typeId", required = false) Long typeId,
            @Parameter(description = "城市编码") @RequestParam(value = "cityCode", required = false) String cityCode) {
        
        return CommonResult.success(shopAreaService.queryAreaList(typeId, cityCode));
    }

    @Operation(summary = "ES全文搜索商户", description = "基于 ElasticSearch 的商户全文检索，支持分词、高亮、距离排序")
    @GetMapping("/search")
    public CommonResult<PageResult<ShopListVO>> searchShops(
            @Parameter(description = "搜索关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "商户类型ID") @RequestParam(value = "typeId", required = false) Long typeId,
            @Parameter(description = "商圈区域") @RequestParam(value = "area", required = false) String area,
            @Parameter(description = "用户经度") @RequestParam(value = "longitude", required = false) Double longitude,
            @Parameter(description = "用户纬度") @RequestParam(value = "latitude", required = false) Double latitude,
            @Parameter(description = "最低人均（元）") @RequestParam(value = "minPrice", required = false) Integer minPrice,
            @Parameter(description = "最高人均（元）") @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
            @Parameter(description = "页码") @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页条数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        Long userId = null;
        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            userId = loginUser.getUserId();
        }
        
        if (shopSearchService == null) {
            log.warn("ES 搜索服务未启用");
            return CommonResult.success(new PageResult<>(List.of(), 0L));
        }
        return CommonResult.success(shopSearchService.searchShops(
                keyword, typeId, area, longitude, latitude, minPrice, maxPrice, pageNo, pageSize, userId));
    }

    @Operation(summary = "手动同步商户到ES（管理用）")
    @PostMapping("/es/sync/{shopId}")
    public CommonResult<Void> syncShopToEs(
            @PathVariable(required = false) @Parameter(description = "商户ID，不传则全量同步") Long shopId) {
        if (shopEsSyncService == null) {
            log.warn("ES 同步服务未启用");
            return CommonResult.success(null);
        }
        if (shopId != null) {
            shopEsSyncService.syncShopToEs(shopId);
        } else {
            shopEsSyncService.syncAllShopsToEs();
        }
        return CommonResult.success(null);
    }
}
