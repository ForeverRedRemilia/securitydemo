package com.foreverredremilia.repserver.filter;

import com.foreverredremilia.repserver.security.AESUtil;
import com.foreverredremilia.repserver.security.GetCryptAnnotation;
import com.foreverredremilia.repserver.security.KeyConstant;
import com.foreverredremilia.repserver.security.RSAUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHeadersBody {

    private static final Logger log = LoggerFactory.getLogger(RequestHeadersBody.class);

    private static final Gson gson = new Gson();

    //自定义请求body
    public static Map<String, Object> getBodyContent(String body, String clazz) {
        String decrypt = AESUtil.decrypt(body, KeyConstant.AES_KEY, KeyConstant.SALT);
        Map<String, Object> map = gson.fromJson(decrypt, HashMap.class);
        List<String> crypts = null;
        map.put("decrypt", false);
        try {
            crypts = GetCryptAnnotation.getCrypt(Class.forName(RSAUtil.decrypt(clazz, KeyConstant.PRIVATE_KEY)));
        } catch (ClassNotFoundException e) {
            map.put("status", "701");
            map.put("msg", "class not found!");
            e.printStackTrace();
            return map;
        }
        for (String crypt : crypts) {
            decrypt = RSAUtil.decrypt((String) map.get(crypt), KeyConstant.PRIVATE_KEY);
            if (null == decrypt) {
                map.clear();
                map.put("status", "703");
                map.put("msg", "参数" + crypt + "解密失败");
                map.put("decrypt", false);
                return map;
            }
            map.put(crypt, decrypt);
        }
        map.put("decrypt", true);
        return map;
    }

}
