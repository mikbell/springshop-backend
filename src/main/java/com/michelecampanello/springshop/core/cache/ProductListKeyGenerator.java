package com.michelecampanello.springshop.core.cache;

import org.jspecify.annotations.NonNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component("productListKeyGenerator")
public class ProductListKeyGenerator implements KeyGenerator {

    private static final String KEY_VERSION = "v2";

    @NonNull
    @Override
    public Object generate(@NonNull Object target, @NonNull Method method, Object @NonNull ... params) {
        String paramsKey = Arrays.stream(params)
                .map(Object::toString)
                .collect(Collectors.joining("::"));
        return KEY_VERSION + "::" + paramsKey;
    }
}
