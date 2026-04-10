package com.yuexiang.voucher.constants;

public interface SeckillRedisConstants {

    String SECKILL_LIST_KEY = "seckill:list:";

    String SECKILL_STOCK_KEY = "seckill:stock:";

    String SECKILL_ORDER_KEY = "seckill:order:";

    String SECKILL_ORDER_RESULT_KEY = "seckill:order:result:";

    String SECKILL_SESSION_KEY = "seckill:session:";

    String SECKILL_VOUCHER_KEY = "seckill:voucher:";

    String SECKILL_RETRY_QUEUE_KEY = "seckill:retry:queue";

    String SECKILL_BUTTON_KEY = "seckill:button:";

    long CACHE_TTL_MINUTES = 5;

    long ORDER_RESULT_TTL_MINUTES = 30;

    long BUTTON_TTL_SECONDS = 5;
}
