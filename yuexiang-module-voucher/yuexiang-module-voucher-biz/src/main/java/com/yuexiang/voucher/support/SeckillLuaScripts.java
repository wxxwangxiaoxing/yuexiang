package com.yuexiang.voucher.support;

public final class SeckillLuaScripts {

    private SeckillLuaScripts() {}

    /**
     * 秒杀脚本（ARGV 传参）
     * ARGV[1]=voucherId, ARGV[2]=userId
     * 返回: 1 成功 / -1 库存不足 / -2 未预热 / -3 重复购买
     */
    public static final String SECKILL_SCRIPT = """
            local voucherId = ARGV[1]
            local userId = ARGV[2]
            local stockKey = 'seckill:stock:' .. voucherId
            local orderKey = 'seckill:order:' .. voucherId
            local stock = tonumber(redis.call('GET', stockKey))
            if stock == nil then
                return -2
            end
            if stock <= 0 then
                return -1
            end
            if redis.call('SISMEMBER', orderKey, userId) == 1 then
                return -3
            end
            redis.call('DECR', stockKey)
            redis.call('SADD', orderKey, userId)
            return 1
            """;

    /**
     * 秒杀下单脚本（KEYS 传参）
     * KEYS[1]=stockKey, KEYS[2]=orderKey, ARGV[1]=userId
     * 返回: 0 成功 / -1 库存不足 / -2 重复购买 / -3 活动不存在
     */
    public static final String SECKILL_ORDER_SCRIPT = """
            local stock = tonumber(redis.call('GET', KEYS[1]))
            if stock == nil then return -3 end
            if stock <= 0   then return -1 end
            if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then return -2 end
            redis.call('DECR', KEYS[1])
            redis.call('SADD', KEYS[2], ARGV[1])
            return 0
            """;
}
