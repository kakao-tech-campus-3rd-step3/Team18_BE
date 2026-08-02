local current = redis.call('ZSCORE', KEYS[1], ARGV[1])
if current ~= false and tonumber(current) == tonumber(ARGV[2]) then
    return redis.call('ZREM', KEYS[1], ARGV[1])
end
return 0
