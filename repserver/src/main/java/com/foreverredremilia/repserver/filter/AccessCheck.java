package com.foreverredremilia.repserver.filter;

import com.foreverredremilia.repserver.redis.RedisUtil;
import com.foreverredremilia.repserver.security.KeyConstant;
import com.foreverredremilia.repserver.security.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
            map.put("status", "869");
            map.put("msg", "Headers中缺少timestamp！");
            return map;
        }
        String timestamp = RSAUtil.decrypt(list.get(0), KeyConstant.PRIVATE_KEY);
        if (null == timestamp){
            map.put("status", "871");
            map.put("msg", "timestamp解密失败！");
            return map;
        }
        if (!expire(Long.parseLong(timestamp))) {
            map.put("status", "873");
            map.put("msg", "会话超时！");
            return map;
        }
        list = headers.get("token");
        if (null == list) {
            map.put("status", "875");
            map.put("msg", "Headers中缺少token");
            return map;
        }
        if (StringUtils.isEmpty(bodyToken)) {
            map.put("status", "877");
            map.put("msg", "Body中缺少token");
            return map;
        }
        bodyToken = RSAUtil.decrypt(bodyToken,KeyConstant.PRIVATE_KEY);
        if (null == bodyToken){
            map.put("status", "879");
            map.put("msg", "bodyToken解密失败");
            return map;
        }
        String token = RSAUtil.decrypt(list.get(0), KeyConstant.PRIVATE_KEY);
        if (null == token){
            map.put("status", "881");
            map.put("msg", "token解密失败！");
            return map;
        }
        if (!bind(token, bodyToken)) {
            map.put("status", "883");
            map.put("msg", "token绑定不一致！");
            return map;
        }
        if (!attack(list.get(0))) {
            map.put("status", "885");
            map.put("msg", "检测到重放攻击！");
            return map;
        }
        map.put("access", true);
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

}
