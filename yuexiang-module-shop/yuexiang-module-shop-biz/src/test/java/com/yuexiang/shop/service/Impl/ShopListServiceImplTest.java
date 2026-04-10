package com.yuexiang.shop.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.common.enums.ResultCodeEnum;
import com.yuexiang.common.exception.BusinessException;
import com.yuexiang.shop.domain.dto.ShopListQueryDTO;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.ShopListPageVO;
import com.yuexiang.shop.mapper.ShopMapper;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopListServiceImplTest {

    @Mock
    private ShopMapper shopMapper;

    @Mock
    private ShopTypeMapper shopTypeMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ShopListServiceImpl shopListService;

    private ShopListQueryDTO query;
    private Shop testShop;

    @BeforeEach
    void setUp() {
        query = new ShopListQueryDTO();
        query.setPageNo(1);
        query.setPageSize(10);
        query.setSortBy("score");

        testShop = new Shop();
        testShop.setId(1L);
        testShop.setName("测试商户");
        testShop.setTypeId(1L);
        testShop.setArea("望京");
        testShop.setAddress("测试地址");
        testShop.setLongitude(new BigDecimal("116.481"));
        testShop.setLatitude(new BigDecimal("39.997"));
        testShop.setScore(new BigDecimal("4.8"));
        testShop.setAvgPrice(8500);
        testShop.setSalesCount(100);
        testShop.setCommentCount(50);
        testShop.setImages("[\"img1.jpg\",\"img2.jpg\"]");
    }

    @Test
    @DisplayName("商户列表查询 - 正常流程")
    void queryShopList_Success() {
        when(shopMapper.selectShopListBasic(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(testShop));
        when(shopMapper.countShopList(any(), any(), any(), any(), any())).thenReturn(1L);
        when(shopMapper.selectShopTags(anyLong())).thenReturn(List.of("川菜", "朋友聚餐"));
        when(shopTypeMapper.selectById(anyLong())).thenReturn(createShopType());
        when(shopMapper.selectAiSummary(anyLong())).thenReturn("AI测试摘要");

        ShopListPageVO result = shopListService.queryShopList(query, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("测试商户", result.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("商户列表查询 - 按距离排序")
    void queryShopList_SortByDistance() {
        query.setSortBy("distance");
        query.setLongitude(116.481);
        query.setLatitude(39.997);
        testShop.setDistance(new BigDecimal("500"));

        when(shopMapper.selectShopListWithDistance(any(), any(), any(), any(), any(), anyDouble(), anyDouble(),
                any(), anyInt(), anyInt())).thenReturn(List.of(testShop));
        when(shopMapper.countShopList(any(), any(), any(), any(), any())).thenReturn(1L);
        when(shopMapper.selectShopTags(anyLong())).thenReturn(Collections.emptyList());
        when(shopTypeMapper.selectById(anyLong())).thenReturn(createShopType());

        ShopListPageVO result = shopListService.queryShopList(query, null);

        assertNotNull(result);
        assertEquals(500, result.getRecords().get(0).getDistance());
    }

    @Test
    @DisplayName("商户列表查询 - 距离排序但未传经纬度")
    void queryShopList_DistanceWithoutCoordinates() {
        query.setSortBy("distance");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> shopListService.queryShopList(query, null));

        assertEquals(ResultCodeEnum.BAD_REQUEST.getCode(), exception.getCode());
        assertEquals("排序方式选择距离时经纬度不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("商户列表查询 - 无效排序方式")
    void queryShopList_InvalidSortBy() {
        query.setSortBy("invalid");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> shopListService.queryShopList(query, null));

        assertEquals(ResultCodeEnum.BAD_REQUEST.getCode(), exception.getCode());
        assertEquals("无效的排序方式", exception.getMessage());
    }

    @Test
    @DisplayName("商户列表查询 - pageSize超限")
    void queryShopList_PageSizeExceeded() {
        query.setPageSize(30);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> shopListService.queryShopList(query, null));

        assertEquals(ResultCodeEnum.BAD_REQUEST.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("pageSize 不能超过"));
    }

    @Test
    @DisplayName("商户列表查询 - 关键词过长")
    void queryShopList_KeywordTooLong() {
        query.setKeyword("a".repeat(51));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> shopListService.queryShopList(query, null));

        assertEquals(ResultCodeEnum.BAD_REQUEST.getCode(), exception.getCode());
        assertEquals("搜索关键词长度不能超过50", exception.getMessage());
    }

    @Test
    @DisplayName("商户列表查询 - 空列表")
    void queryShopList_EmptyList() {
        when(shopMapper.selectShopListBasic(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(shopMapper.countShopList(any(), any(), any(), any(), any())).thenReturn(0L);

        ShopListPageVO result = shopListService.queryShopList(query, null);

        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    private ShopType createShopType() {
        ShopType shopType = new ShopType();
        shopType.setId(1L);
        shopType.setName("美食");
        return shopType;
    }
}
