package com.example.reqserver.security;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GetCryptAnnotation {

    public static List<String> getCrypt(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<String> list = new ArrayList<>();
        for (Field field : fields) {
            if (null != field.getAnnotation(Crypt.class)
                    && field.getAnnotation(Crypt.class).isCrypt()) {
                list.add(field.getName());
            }
        }
        return list;
    }

}
