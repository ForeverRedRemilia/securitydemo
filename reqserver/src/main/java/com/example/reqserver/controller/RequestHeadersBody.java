package com.example.reqserver.controller;

import com.example.reqserver.security.AESUtil;
import com.example.reqserver.security.GetCryptAnnotation;
import com.example.reqserver.security.KeyConstant;
import com.example.reqserver.security.RSAUtil;
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

    /**
     * 获取加密后的 Body
     *
     * @param map   由 json字符串转换的 map
     * @param clazz json字符串转换之前对应的 dto类
     * @return
     */
    public static String getBodyContent(Map<String, Object> map, Class<?> clazz, String token) {
        List<String> crypts = GetCryptAnnotation.getCrypt(clazz);
        for (String key : crypts) {
            //使用响应端的公钥加密敏感数据
            String encrypt = RSAUtil.encrypt(String.valueOf(map.get(key)), KeyConstant.REP_PUB_KEY);
            map.put(key, encrypt);
        }
        //整个RequestBody：将业务数据与token进行捆绑获得
        Map<String, Object> bodyMap = new HashMap<>();
        //使用AES密钥加密封业务数据
        bodyMap.put("body", AESUtil.encrypt(gson.toJson(map), KeyConstant.AES_KEY, KeyConstant.SALT));
        //使用Gateway的公钥加密token
        bodyMap.put("token", RSAUtil.encrypt(token, KeyConstant.GATE_PUB_KEY));
        //使用AES密钥加密整个RequestBody
        return AESUtil.encrypt(gson.toJson(bodyMap), KeyConstant.AES_KEY, KeyConstant.SALT);
    }

    //自定义获取请求头部
    public static HttpHeaders getGatewayHeader(String token,String appId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //使用Gateway的公钥加密token和timestamp，并添加到请求头部
        headers.add("token"
                , RSAUtil.encrypt(token, KeyConstant.GATE_PUB_KEY));
        headers.add("timestamp"
                , RSAUtil.encrypt(String.valueOf(System.currentTimeMillis()), KeyConstant.GATE_PUB_KEY));
        headers.add("appId"
                , RSAUtil.encrypt(appId, KeyConstant.GATE_PUB_KEY));
        return headers;
    }

    //生成UUID
    public static String token() {
        return UUID.randomUUID().toString();
    }

}
