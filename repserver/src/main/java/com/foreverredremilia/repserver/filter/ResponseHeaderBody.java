package com.foreverredremilia.repserver.filter;


import com.foreverredremilia.repserver.security.AESUtil;
import com.foreverredremilia.repserver.security.GetCryptAnnotation;
import com.foreverredremilia.repserver.security.KeyConstant;
import com.foreverredremilia.repserver.security.RSAUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ResponseHeaderBody {

    private static final Logger log = LoggerFactory.getLogger(ResponseHeaderBody.class);

    private static final Gson gson = new Gson();

    public static String getBody(Map<String, Object> map,String token){
        //获取需要加密的字段
        List<String> crypts = null;
        try {
            crypts = GetCryptAnnotation.getCrypt(Class.forName(String.valueOf(map.get("clazz"))));
        } catch (Exception e) {
            map.clear();
            map.put("status", "601");
            map.put("msg", "class not found!");
            e.printStackTrace();
        }
        map.remove("clazz");
        Map<String,Object> body = new HashMap<>();
        if (null != crypts) {
            for (String key : crypts) {
                //RSA加密
                String encrypt = RSAUtil.encrypt(String.valueOf(map.get(key)), KeyConstant.REQ_PUB_KEY);
                if (null == encrypt) {
                    map.clear();
                    map.put("status", "603");
                    map.put("msg", "响应端加密参数"+key+"失败！");
                    return fillResp(map,token);
                }
                body.put(key, encrypt);
            }
        }
        map.put("body",AESUtil.encrypt(gson.toJson(body), KeyConstant.AES_KEY, KeyConstant.SALT));
        map.put("status","300");
        return fillResp(map,token);
    }

    //最终填充response，响应错误时可直接调用
    public static String fillResp(Map<String, Object> map, String token) {
        map.put("token", RSAUtil.encrypt(token, KeyConstant.GATE_PUB_KEY));
        return AESUtil.encrypt(gson.toJson(map), KeyConstant.AES_KEY, KeyConstant.SALT);
    }

    //自定义请求headers
    public static void setHeaders(HttpHeaders headers, String token) {
        //使用Gateway的公钥加密token和timestamp，并添加到请求头部
        headers.set("token"
                , RSAUtil.encrypt(token, KeyConstant.GATE_PUB_KEY));
        headers.set("timestamp"
                , RSAUtil.encrypt(String.valueOf(System.currentTimeMillis()), KeyConstant.GATE_PUB_KEY));
    }

    public static String token() {
        return UUID.randomUUID().toString();
    }

}
