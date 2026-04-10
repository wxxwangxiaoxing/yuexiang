-- unlike.lua
-- KEYS[1] = blog:liked:{blogId}
-- KEYS[2] = blog:like:count:{blogId}
-- ARGV[1] = userId

if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 0 then
    return 0
end

redis.call('SREM', KEYS[1], ARGV[1])

redis.call('DECR', KEYS[2])

return 1
