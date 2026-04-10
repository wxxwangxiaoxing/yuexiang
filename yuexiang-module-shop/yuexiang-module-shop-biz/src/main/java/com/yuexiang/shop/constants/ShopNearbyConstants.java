package com.yuexiang.shop.constants;

public final class ShopNearbyConstants {

    private ShopNearbyConstants() {
    }

    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 20;
    public static final int DB_BATCH_SIZE = 50;
    public static final int GEO_FETCH_MULTIPLIER = 3;

    public static final double MAX_RADIUS_KM = 10.0;
    public static final double MAX_RADIUS_METERS = MAX_RADIUS_KM * 1000;
    public static final double EARTH_RADIUS_METERS = 6371000.0;
    public static final double METERS_PER_DEGREE_LAT = 111_000.0;
    public static final double CURSOR_TOLERANCE_METERS = 0.1;

    public static final double MIN_CHINA_LNG = 73.0;
    public static final double MAX_CHINA_LNG = 136.0;
    public static final double MIN_CHINA_LAT = 3.0;
    public static final double MAX_CHINA_LAT = 54.0;
}
