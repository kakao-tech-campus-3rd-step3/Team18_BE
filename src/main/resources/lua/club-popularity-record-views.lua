local status = redis.call('GET', KEYS[5])
if status ~= 'READY' then
    return 3
end

if redis.call('EXISTS', KEYS[6]) == 1 then
    return 2
end

redis.call('SET', KEYS[6], '1', 'EX', ARGV[4], 'NX')
redis.call('ZADD', KEYS[1], 'GT', ARGV[1], ARGV[2])
redis.call('ZADD', KEYS[2], 'GT', ARGV[1], ARGV[2])
redis.call('ZADD', KEYS[3], 'GT', ARGV[1], ARGV[3])
redis.call('SADD', KEYS[4], string.match(ARGV[3], '^v1|([^|]+)|'))
redis.call('EXPIRE', KEYS[1], ARGV[6])
redis.call('EXPIRE', KEYS[2], ARGV[5])
return 1
