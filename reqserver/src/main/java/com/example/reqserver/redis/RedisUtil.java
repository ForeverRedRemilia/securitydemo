package com.example.reqserver.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtil {

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Boolean setToken(String key, String value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(10));
    }

    public void set(String key,String value){
        redisTemplate.opsForValue().set(key,value);
    }

}
