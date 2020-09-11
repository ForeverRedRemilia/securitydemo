package com.example.gateway.filter;


import com.example.gateway.security.AESUtil;
import com.example.gateway.security.KeyConstant;
import com.example.gateway.security.RSAUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ResponseHeaderBody {

    private static final Logger log = LoggerFactory.getLogger(ResponseHeaderBody.class);

    private static final Gson gson = new Gson();

    public static String getBody(Map<String, Object> map,String token){
        map.put("status","300");
        return fillResp(map,token);
    }

    //最终填充response，响应错误时可直接调用
    public static String fillResp(Map<String, Object> map, String token) {
        map.put("token", RSAUtil.encrypt(token, KeyConstant.REQ_PUB_KEY));
        return AESUtil.encrypt(gson.toJson(map), KeyConstant.AES_KEY, KeyConstant.SALT);
    }

    //自定义请求headers
    public static void setHeaders(HttpHeaders headers, String token) {
        //使用Gateway的公钥加密token和timestamp，并添加到请求头部
        headers.set("token"
                , RSAUtil.encrypt(token, KeyConstant.REQ_PUB_KEY));
        headers.set("timestamp"
                , RSAUtil.encrypt(String.valueOf(System.currentTimeMillis()), KeyConstant.REQ_PUB_KEY));
    }

    public static String token() {
        return UUID.randomUUID().toString();
    }

}
