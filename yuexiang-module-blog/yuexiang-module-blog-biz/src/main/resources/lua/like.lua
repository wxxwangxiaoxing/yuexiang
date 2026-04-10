-- like.lua
-- KEYS[1] = blog:liked:{blogId}
-- KEYS[2] = blog:like:count:{blogId}
-- ARGV[1] = userId

if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
    return 0
end

redis.call('SADD', KEYS[1], ARGV[1])

redis.call('INCR', KEYS[2])

return 1
