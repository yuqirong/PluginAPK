package me.yuqirong.plugin.util;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Zhouyu
 * @date 2018/8/13
 */
public class ReflectUtil {


    private ReflectUtil() {
        //no instance
    }

    public static Object invokeMethod(String className, Object instance, String methodName, Class<?>[] argsClass, Object[] args) {
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return null;
        }
        try {
            Class<?> threadClazz = Class.forName(className);
            Method method = threadClazz.getMethod(methodName, argsClass);
            return method.invoke(instance, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getField(Class<?> clazz, Object object, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setField(Class<?> clazz, Object object, String fieldName, Object fieldValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, fieldValue);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
