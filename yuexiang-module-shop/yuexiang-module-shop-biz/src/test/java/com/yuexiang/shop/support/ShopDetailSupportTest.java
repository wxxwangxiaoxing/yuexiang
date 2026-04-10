package com.yuexiang.shop.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexiang.shop.domain.entity.Shop;
import com.yuexiang.shop.domain.entity.ShopType;
import com.yuexiang.shop.domain.vo.ShopDetailVO;
import com.yuexiang.shop.mapper.ReviewMapper;
import com.yuexiang.shop.mapper.ShopTypeMapper;
import com.yuexiang.shop.service.FavoriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopDetailSupportTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ShopTypeMapper shopTypeMapper;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private FavoriteService favoriteService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ShopDetailSupport shopDetailSupport;

    @Test
    void buildShopDetailVoMapsPublicFieldsCorrectly() {
        Shop shop = new Shop();
        shop.setId(1L);
        shop.setName("测试商户");
        shop.setTypeId(2L);
        shop.setImages("[\"a.jpg\",\"b.jpg\"]");
        shop.setArea("望京");
        shop.setAddress("测试地址");
        shop.setLongitude(new BigDecimal("116.481"));
        shop.setLatitude(new BigDecimal("39.997"));
        shop.setAvgPrice(8500);
        shop.setScore(new BigDecimal("4.8"));
        shop.setOpenHours("{\"mon\":\"09:00-18:00\"}");

        ShopType shopType = new ShopType();
        shopType.setName("美食");
        when(shopTypeMapper.selectById(2L)).thenReturn(shopType);
        when(reviewMapper.countPublishedByShopId(1L)).thenReturn(12);

        ShopDetailVO result = shopDetailSupport.buildShopDetailVO(shop);

        assertEquals("测试商户", result.getName());
        assertEquals("美食", result.getTypeName());
        assertEquals(2, result.getImages().size());
        assertEquals("85.00", result.getAvgPrice());
        assertEquals(4.8D, result.getScore());
        assertEquals(116.481D, result.getLongitude());
        assertEquals(39.997D, result.getLatitude());
        assertEquals(12, result.getReviewCount());
    }

    @Test
    void enrichDynamicFieldsSetsFavoriteAndOpenStatus() throws Exception {
        ShopDetailVO shopDetailVO = new ShopDetailVO();
        shopDetailVO.setId(1L);

        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        String dayKey = today.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase();
        String openHours = objectMapper.writeValueAsString(Map.of(
                dayKey,
                now.minusHours(1).format(DateTimeFormatter.ofPattern("HH:mm")) + "-"
                        + now.plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"))
        ));
        shopDetailVO.setOpenHours(openHours);

        when(favoriteService.isFavorite(1001L, 1L)).thenReturn(true);

        shopDetailSupport.enrichDynamicFields(shopDetailVO, 1001L);

        assertTrue(shopDetailVO.getIsFavorite());
        assertTrue(shopDetailVO.getIsOpen());
    }

    @Test
    void enrichDynamicFieldsDefaultsToNotFavoriteForAnonymousUser() {
        ShopDetailVO shopDetailVO = new ShopDetailVO();
        shopDetailVO.setId(2L);
        shopDetailVO.setOpenHours("not-json");

        shopDetailSupport.enrichDynamicFields(shopDetailVO, null);

        assertFalse(shopDetailVO.getIsFavorite());
        assertTrue(shopDetailVO.getIsOpen());
    }
}
