package com.littlewin.common.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
    }

    public Long incr(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    public Long incr(String key, long delta, long timeout, TimeUnit unit) {
        Long result = stringRedisTemplate.opsForValue().increment(key, delta);
        if (result != null && result == delta) {
            stringRedisTemplate.expire(key, timeout, unit);
        }
        return result;
    }

    public Long decr(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, -delta);
    }

    public boolean setNx(String key, String value, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(
            stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit)
        );
    }
}
