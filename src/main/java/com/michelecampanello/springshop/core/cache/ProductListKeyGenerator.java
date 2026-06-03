package com.michelecampanello.springshop.core.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component("productListKeyGenerator")
public class ProductListKeyGenerator implements KeyGenerator {

    private static final String KEY_VERSION = "v2";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String paramsKey = Arrays.stream(params)
                .map(Object::toString)
                .collect(Collectors.joining("::"));
        return KEY_VERSION + "::" + paramsKey;
    }
}
