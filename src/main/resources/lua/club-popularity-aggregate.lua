local status = redis.call('GET', KEYS[4])
if status ~= 'READY' then
    return 0
end

redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[2])
redis.call('ZREMRANGEBYSCORE', KEYS[2], '-inf', ARGV[3])
local recent = redis.call('ZCOUNT', KEYS[1], '(' .. ARGV[2], '+inf')
local active = redis.call('ZCOUNT', KEYS[2], '(' .. ARGV[3], '+inf')
if recent == 0 and active == 0 then
    redis.call('SREM', KEYS[3], ARGV[4])
end
return recent * 4294967296 + active
