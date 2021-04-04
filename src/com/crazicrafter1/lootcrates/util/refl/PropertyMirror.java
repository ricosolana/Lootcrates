package com.crazicrafter1.lootcrates.util.refl;

import com.crazicrafter1.lootcrates.util.ReflectionUtil;

import java.lang.reflect.Constructor;

public class PropertyMirror {

    private static final Class<?> propertyClass = ReflectionUtil.getCanonicalClass("com.mojang.authlib.properties.Property");

    private final static Constructor<?> propertyConstructor = ReflectionUtil.getConstructor(propertyClass,
            String.class, String.class, String.class);

    private Object instance;

    public PropertyMirror(String name, String value, String signature) {
        instance = ReflectionUtil.invokeConstructor(propertyConstructor, name, value, signature);
    }

    public Object getInstance() {
        return instance;
    }

}
