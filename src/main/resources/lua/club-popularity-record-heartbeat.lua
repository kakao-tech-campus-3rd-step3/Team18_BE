local status = redis.call('GET', KEYS[3])
if status ~= 'READY' then
    return 3
end

if redis.call('SISMEMBER', KEYS[5], ARGV[5]) == 0 then
    return 4
end

if redis.call('EXISTS', KEYS[4]) == 1 then
    return 2
end

redis.call('SET', KEYS[4], '1', 'EX', ARGV[3])
redis.call('ZADD', KEYS[1], 'GT', ARGV[1], ARGV[2])
redis.call('SADD', KEYS[2], ARGV[5])
redis.call('EXPIRE', KEYS[1], ARGV[4])
return 1
