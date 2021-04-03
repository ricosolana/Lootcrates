package com.crazicrafter1.lootcrates;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtil {

    private final static String CRAFTBUKKIT = Bukkit.getServer().getClass().getPackage().getName();
    private final static String NMS = "net.minecraft.server." + CRAFTBUKKIT.substring(23);

    // Not instantiable
    private ReflectionUtil() { }

    // get class by package dir
    private static @NotNull Class getCanonicalClass(final @NotNull String canonicalName) {
        try {
            return Class.forName(canonicalName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot find " + canonicalName, e);
        }
    }

    public static Class<?> getCraftClass(String name) {
        return getCanonicalClass(CRAFTBUKKIT + "." + name);
    }

    public static Class<?> getMinecraftClass(String name) {
        return getCanonicalClass(NMS + "." + name);
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?>... params) {
        try {
            return clazz.getDeclaredMethod(method, params);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot find " + method, e);
        }
    }

    public static Object invokeStaticMethod(Method method, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(Method method, Object instance, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(instance, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
