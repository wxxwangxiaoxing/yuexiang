-- rollback_like.lua
-- KEYS[1] = blog:liked:{blogId}
-- KEYS[2] = blog:like:count:{blogId}
-- ARGV[1] = userId
-- ARGV[2] = "LIKE" or "UNLIKE"

if ARGV[2] == 'LIKE' then
    redis.call('SREM', KEYS[1], ARGV[1])
    redis.call('DECR', KEYS[2])
else
    redis.call('SADD', KEYS[1], ARGV[1])
    redis.call('INCR', KEYS[2])
end

return 1
