package com.example.gateway.filter;

import com.example.gateway.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AccessCheck {
    private RedisUtil redisUtil;
    private static final long EXPIRE_TIME = 10;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public Map<String, Object> accessCheck(HttpHeaders headers,String bodyToken) {
        Map<String, Object> map = new HashMap<>();
        List<String> list = headers.get("timestamp");
        map.put("access",false);
        if (null == list) {
            map.put("status","971");
            map.put("msg","Headers中缺少timestamp！");
            return map;
        }
        if (!expire(Long.parseLong(list.get(0)))){
            map.put("status","973");
            map.put("msg","会话超时！");
            return map;
        }
        list = headers.get("token");
        if (null == list){
            map.put("status","975");
            map.put("msg","Headers中缺少token");
            return map;
        }
        if (StringUtils.isEmpty(bodyToken)){
            map.put("status","977");
            map.put("msg","Body中缺少token");
            return map;
        }
        String token = list.get(0);
        if (!attack(list.get(0))){
            map.put("status","979");
            map.put("msg","检测到重放攻击！");
            return map;
        }
        list = headers.get("appId");
        if (null == list){
            map.put("status","981");
            map.put("msg","Headers中缺少appId");
            return map;
        }
        String path = path(list.get(0));
        if (StringUtils.isEmpty(path)){
            map.put("status","983");
            map.put("msg","appId无对应地址！");
            return map;
        }
        map.put("access",true);
        map.put("path",path);
        return map;
    }

    private boolean expire(long l) {
        return EXPIRE_TIME > System.currentTimeMillis() - l;
    }

    private boolean attack(String token) {
        Boolean aBoolean = redisUtil.setToken(token, "");
        return null != aBoolean && aBoolean;
    }

    private boolean bind(String token1,String token2){
        return token1.equals(token2);
    }

    private String path(String appId) {
        return redisUtil.get(appId);
    }

}
