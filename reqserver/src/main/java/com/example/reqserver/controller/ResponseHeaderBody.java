package com.example.reqserver.controller;

import com.example.reqserver.redis.RedisUtil;
import com.example.reqserver.security.AESUtil;
import com.example.reqserver.security.GetCryptAnnotation;
import com.example.reqserver.security.KeyConstant;
import com.example.reqserver.security.RSAUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResponseHeaderBody {

    private static final Logger log = LoggerFactory.getLogger(ResponseHeaderBody.class);

    private static final Gson gson = new Gson();

    private RedisUtil redisUtil;
    private static ResponseHeaderBody responseHeaderBody;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @PostConstruct
    public void init() {
        responseHeaderBody = this;
        redisUtil = this.redisUtil;
    }

    public static String getBody(ResponseEntity<String> responseEntity, Class<?> clazz) {
        HttpHeaders headers = responseEntity.getHeaders();
        //对timestamp校验
        List<String> list = headers.get("timestamp");
        if (null == list) {
            return "解密失败，Headers参数缺少timestamp";
        } else {
            String s = list.get(0);
            String timestamp = RSAUtil.decrypt(s, KeyConstant.PRIVATE_KEY);
            if (null == timestamp) {
                return "解密失败，timestamp可能使用了错误的公钥加密";
            }
            long l = Long.parseLong(timestamp);
            long l1 = System.currentTimeMillis();
            if (10000 < l1 - l) {
                return "会话超时！";
            }
        }
        //解密整体Json，拿到Body中的 token
        Map<String, Object> bodyMap = gson.fromJson(AESUtil.decrypt(responseEntity.getBody(),
                KeyConstant.AES_KEY, KeyConstant.SALT), HashMap.class);
        String bodyToken = RSAUtil.decrypt(String.valueOf(bodyMap.get("token")),
                KeyConstant.PRIVATE_KEY);
        //对token进行校验
        list = headers.get("token");
        if (null == list) {
            return "解密失败，Headers参数缺少token";
        } else {
            String s = list.get(0);
            String token = RSAUtil.decrypt(s, KeyConstant.PRIVATE_KEY);
            if (null == token) {
                return "解密失败, token可能使用了错误的公钥加密";
            }
            if (!token.equals(bodyToken)) {
                return "解密失败，Headers中的token与Body中的不一致";
            }
            Boolean aBoolean = responseHeaderBody.redisUtil.setToken(token, "");
            if (null == aBoolean || !aBoolean) {
                return "监测到重放攻击！";
            }
        }
        //对业务数据进行解密
        Map<String,Object> body = gson.fromJson(AESUtil.decrypt(String.valueOf(bodyMap.get("body")),
                KeyConstant.AES_KEY,KeyConstant.SALT),HashMap.class);
        List<String> crypts = GetCryptAnnotation.getCrypt(clazz);
        for (String key : crypts) {
            //使用私钥解密敏感数据
            String decrypt = RSAUtil.decrypt(String.valueOf(body.get(key)), KeyConstant.PRIVATE_KEY);
            body.put(key, decrypt);
        }
        return gson.toJson(body);
    }

}
