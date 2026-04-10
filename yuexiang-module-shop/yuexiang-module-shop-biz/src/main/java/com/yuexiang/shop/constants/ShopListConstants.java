package com.yuexiang.shop.constants;

import java.util.Set;

public final class ShopListConstants {

    private ShopListConstants() {
    }

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 20;
    public static final int MAX_KEYWORD_LENGTH = 50;
    public static final int MAX_IMAGE_COUNT = 3;
    public static final String DEFAULT_SORT_BY = "score";
    public static final String DISTANCE_SORT_BY = "distance";

    public static final Set<String> VALID_SORT_BY = Set.of(
            DISTANCE_SORT_BY,
            "score",
            "price_asc",
            "price_desc",
            "ai"
    );
}
