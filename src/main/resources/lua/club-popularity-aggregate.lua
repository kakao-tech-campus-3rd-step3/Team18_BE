local status = redis.call('GET', KEYS[4])
if status ~= 'READY' then
    return 0
end

local recent = redis.call('ZCOUNT', KEYS[1], '(' .. ARGV[2], '+inf')
local active = redis.call('ZCOUNT', KEYS[2], '(' .. ARGV[3], '+inf')
return recent * 4294967296 + active
