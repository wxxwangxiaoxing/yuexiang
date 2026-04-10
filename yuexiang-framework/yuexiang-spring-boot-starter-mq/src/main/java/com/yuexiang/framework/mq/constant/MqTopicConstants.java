package com.yuexiang.framework.mq.constant;

public final class MqTopicConstants {

    private MqTopicConstants() {}

    public static final String LIKE_EVENT_TOPIC = "LIKE_EVENT_TOPIC";

    public static final String LIKE_EVENT_TAG_LIKE = "like";
    public static final String LIKE_EVENT_TAG_UNLIKE = "unlike";

    public static final String USER_EVENT_TOPIC = "USER_EVENT_TOPIC";
    public static final String USER_EVENT_TAG_REGISTER = "register";
    public static final String USER_EVENT_TAG_CANCEL = "cancel";

    public static final String ORDER_EVENT_TOPIC = "ORDER_EVENT_TOPIC";
    public static final String ORDER_EVENT_TAG_CREATE = "create";
    public static final String ORDER_EVENT_TAG_PAY = "pay";
    public static final String ORDER_EVENT_TAG_CANCEL = "cancel";
}
