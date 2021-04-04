package com.crazicrafter1.lootcrates.util.refl;

import com.crazicrafter1.lootcrates.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class GameProfileMirror {

    public final static Class gameProfileClass = ReflectionUtil.getCanonicalClass("com.mojang.authlib.GameProfile");
    private final static Constructor<?> constructor = ReflectionUtil.getConstructor(gameProfileClass, UUID.class, String.class);

    private final static Field propertyMapField = ReflectionUtil.getField(gameProfileClass, "properties");

    private final static Method putMethod = ReflectionUtil.getMethod(propertyMapField.getType().getSuperclass(), "put", Object.class, Object.class);

    private Object gameProfileInstance;

    public GameProfileMirror(UUID uuid, String string) {
        this.gameProfileInstance = ReflectionUtil.invokeConstructor(constructor, uuid, string);
    }

    public boolean putProperty(String key, PropertyMirror propertyMirror) {
        return (boolean) ReflectionUtil.invokeMethod(putMethod,
                ReflectionUtil.getFieldInstance(propertyMapField, this.gameProfileInstance), key, propertyMirror.getInstance());
    }

    public Object getInstance() {
        return gameProfileInstance;
    }

}
