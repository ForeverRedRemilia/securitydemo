package com.example.gateway.filter;

import com.example.gateway.security.AESUtil;
import com.example.gateway.security.KeyConstant;
import com.example.gateway.security.RSAUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RequestHeadersBody {

    private static final Logger log = LoggerFactory.getLogger(RequestHeadersBody.class);

    private static final Gson gson = new Gson();

    //自定义请求body
    public static String getBodyContent(Map<String, Object> map, String token, String clazz) {
        //使用响应端的公钥加密token
        map.put("token", RSAUtil.encrypt(token, KeyConstant.REP_PUB_KEY));
        //使用响应端的公钥加密clazz
        map.put("clazz", RSAUtil.encrypt(clazz, KeyConstant.REP_PUB_KEY));
        //使用AES密钥加密整个RequestBody
        return AESUtil.encrypt(gson.toJson(map), KeyConstant.AES_KEY, KeyConstant.SALT);
    }

    //自定义请求headers
    public static void setHeaders(HttpHeaders headers, String token) {
        //使用Gateway的公钥加密token和timestamp，并添加到请求头部
        headers.set("token"
                , RSAUtil.encrypt(token, KeyConstant.REP_PUB_KEY));
        headers.set("timestamp"
                , RSAUtil.encrypt(String.valueOf(System.currentTimeMillis()), KeyConstant.REP_PUB_KEY));
    }

    //生成UUID
    public static String token() {
        return UUID.randomUUID().toString();
    }

}
