package com.yuexiang.shop.constants;

public final class ShopAreaConstants {

    private ShopAreaConstants() {
    }

    public static final String AREA_LIST_CACHE_KEY = "shop:area:list";
    public static final long CACHE_TTL_HOURS = 1L;
    public static final String CACHE_ITEM_SEPARATOR = "|";
    public static final String CACHE_FIELD_SEPARATOR = ",";
    public static final int CACHE_ITEM_PARTS = 2;
}
