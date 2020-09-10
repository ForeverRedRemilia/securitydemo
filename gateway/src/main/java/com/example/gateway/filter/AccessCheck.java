package com.example.gateway.filter;

import com.example.gateway.redis.RedisUtil;
import com.example.gateway.security.AESUtil;
import com.example.gateway.security.KeyConstant;
import com.example.gateway.security.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AccessCheck {
    private RedisUtil redisUtil;
    private static final long EXPIRE_TIME = 10000;
    private static AccessCheck accessCheck;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @PostConstruct
    public void init(){
        accessCheck = this;
        redisUtil = this.redisUtil;
    }

    public static Map<String, Object> accessCheck(HttpHeaders headers, String bodyToken) {
        Map<String, Object> map = new HashMap<>();
        List<String> list = headers.get("timestamp");
        map.put("access", false);
        if (null == list) {
            map.put("status", "969");
            map.put("msg", "Headers中缺少timestamp！");
            return map;
        }
        String timestamp = RSAUtil.decrypt(list.get(0), KeyConstant.PRIVATE_KEY);
        if (null == timestamp){
            map.put("status", "971");
            map.put("msg", "timestamp解密失败！");
            return map;
        }
        if (!expire(Long.parseLong(timestamp))) {
            map.put("status", "973");
            map.put("msg", "会话超时！");
            return map;
        }
        list = headers.get("token");
        if (null == list) {
            map.put("status", "975");
            map.put("msg", "Headers中缺少token");
            return map;
        }
        if (StringUtils.isEmpty(bodyToken) || "null".equals(bodyToken)) {
            map.put("status", "977");
            map.put("msg", "Body中缺少token");
            return map;
        }
        bodyToken = RSAUtil.decrypt(bodyToken,KeyConstant.PRIVATE_KEY);
        if (null == bodyToken){
            map.put("status", "979");
            map.put("msg", "bodyToken解密失败");
            return map;
        }
        String token = RSAUtil.decrypt(list.get(0), KeyConstant.PRIVATE_KEY);
        if (null == token){
            map.put("status", "981");
            map.put("msg", "token解密失败！");
            return map;
        }
        if (!bind(token, bodyToken)) {
            map.put("status", "983");
            map.put("msg", "token绑定不一致！");
            return map;
        }
        if (!attack(list.get(0))) {
            map.put("status", "985");
            map.put("msg", "检测到重放攻击！");
            return map;
        }
        list = headers.get("appId");
        if (null == list) {
            map.put("status", "987");
            map.put("msg", "Headers中缺少appId");
            return map;
        }
        String uri = uri(list.get(0));
        if (StringUtils.isEmpty(uri)) {
            map.put("status", "989");
            map.put("msg", "appId无对应地址！");
            return map;
        }
        map.put("access", true);
        map.put("uri", uri);
        return map;
    }

    private static boolean expire(long l) {
        return EXPIRE_TIME > System.currentTimeMillis() - l;
    }

    private static boolean attack(String token) {
        Boolean aBoolean = accessCheck.redisUtil.setToken(token, "");
        return null != aBoolean && aBoolean;
    }

    private static boolean bind(String token1, String token2) {
        return token1.equals(token2);
    }

    private static String uri(String appId) {
        return accessCheck.redisUtil.get(appId);
    }

}
