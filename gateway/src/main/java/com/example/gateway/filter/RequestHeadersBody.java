package com.example.gateway.filter;

import com.example.gateway.security.AESUtil;
import com.example.gateway.security.KeyConstant;
import com.example.gateway.security.RSAUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;
import java.util.UUID;

public class RequestHeadersBody {

    private static final Logger log = LoggerFactory.getLogger(RequestHeadersBody.class);

    private static final Gson gson = new Gson();

    //自定义请求body
    public static String getBodyContent(Map<String, Object> map, String token,
                                        String clazz) {
        //使用响应端的公钥加密token
        map.put("token", RSAUtil.encrypt(token, KeyConstant.REP_PUB_KEY));
        //使用响应端的公钥加密clazz
        map.put("clazz", RSAUtil.encrypt(clazz, KeyConstant.REP_PUB_KEY));
        //使用AES密钥加密整个RequestBody
        return AESUtil.encrypt(gson.toJson(map), KeyConstant.AES_KEY, KeyConstant.SALT);
    }

    //自定义请求headers
    public static void setHeaders(ServerHttpRequest request, String token,
                                  int length) {
        //使用响应端的公钥加密token和timestamp，并添加到请求头部
        request.mutate().header("token", RSAUtil.encrypt(token, KeyConstant.REP_PUB_KEY));
        request.mutate().header("timestamp", RSAUtil.encrypt(String.valueOf(System.currentTimeMillis()),
                        KeyConstant.REP_PUB_KEY));
        //需要重新设置请求头部的长度，否则会导致下游获取数据不完整
        request.mutate().header("Content-Length", String.valueOf(length));
        //移除appId
        request.mutate().header("appId");
        System.out.println(request.getHeaders());
    }

    //生成UUID
    public static String token() {
        return UUID.randomUUID().toString();
    }

}
