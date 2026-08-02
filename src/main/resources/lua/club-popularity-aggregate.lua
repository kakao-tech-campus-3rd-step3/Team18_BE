local status = redis.call('GET', KEYS[4])
if status ~= 'READY' then
    return 0
end

redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[2])
redis.call('ZREMRANGEBYSCORE', KEYS[2], '-inf', ARGV[3])
local recent = redis.call('ZCARD', KEYS[1])
local active = redis.call('ZCARD', KEYS[2])
if recent == 0 and active == 0 then
    redis.call('SREM', KEYS[3], ARGV[4])
end
return recent * 4294967296 + active
