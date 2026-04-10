package com.yuexiang.system.constant;

public final class CaptchaDrawConstants {

    private CaptchaDrawConstants() {
    }

    public static final int CHAR_START_X = 20;
    public static final int CHAR_SPACING = 25;
    public static final int CHAR_BASE_Y = 28;
    public static final int CHAR_Y_OFFSET = 8;
    public static final int CHAR_MAX_ROTATION = 15;
    public static final int BASE_FONT_SIZE = 28;
    public static final int FONT_SIZE_RANGE = 6;
    public static final int NOISE_LINE_COUNT = 4;
    public static final int NOISE_CURVE_COUNT = 2;
    public static final int NOISE_DOT_COUNT = 50;
    public static final int DARK_COLOR_BOUND = 150;
    public static final int LIGHT_COLOR_BOUND = 200;
    public static final int DOT_COLOR_BOUND = 255;
    public static final int ENCODE_BUFFER_SIZE = 1024;

    public static final String IMAGE_FORMAT = "png";
    public static final String BASE64_PREFIX = "data:image/png;base64,";
    public static final String FONT_NAME = "Arial";
    public static final int FONT_STYLE = 1;

    public static final float NOISE_LINE_STROKE_WIDTH = 1.2f;
    public static final float NOISE_CURVE_STROKE_WIDTH = 1.5f;
}
