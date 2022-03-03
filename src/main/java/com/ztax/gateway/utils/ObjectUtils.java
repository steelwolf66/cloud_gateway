package com.ztax.gateway.utils;



import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class ObjectUtils {
    private ObjectUtils() {
    }

    public static boolean isBlank(Object o) {
        return o == null || o instanceof String && StringUtils.hasLength((String) o) || o instanceof Collection && ((Collection) o).isEmpty() || o instanceof Map && ((Map) o).isEmpty() || o.getClass().isArray() && Array.getLength(o) == 0;
    }

    public static boolean isNotBlank(Object o) {
        return !isBlank(o);
    }

    public static <T> T getFieldValue(Object o, String fieldName) throws InvocationTargetException, IllegalAccessException {
        String methodName = ClassUtils.getterName(fieldName, false);
        Method method = null;

        try {
            method = ClassUtils.getMethodByName(o.getClass(), methodName, 0);
        } catch (Exception var6) {
        }

        if (method == null) {
            methodName = ClassUtils.getterName(fieldName, true);

            try {
                method = ClassUtils.getMethodByName(o.getClass(), methodName, 0);
            } catch (Exception e) {

            }
        }

        if (method != null && method.getParameterCount() == 0) {
            Object invoke = method.invoke(o);
            return (T) invoke;
        } else {
            throw new IllegalArgumentException("没有找到[" + fieldName + "]的getter方法");
        }
    }

    public static void setFieldValue(Object o, String fieldName, Object value) throws InvocationTargetException, IllegalAccessException {
        String methodName = ClassUtils.setterName(fieldName, false);
        Method method = null;

        try {
            method = ClassUtils.getMethodByName(o.getClass(), methodName, 1);
        } catch (Exception var7) {
        }

        if (method == null) {
            methodName = ClassUtils.setterName(fieldName, true);

            try {
                method = ClassUtils.getMethodByName(o.getClass(), methodName, 1);
            } catch (Exception var6) {
            }
        }

        if (method == null) {
            throw new IllegalArgumentException("没有找到[" + fieldName + "]的setter方法");
        } else {
            if (value == null) {
                Class<?> cls = method.getParameterTypes()[0];
                if (cls.isPrimitive()) {
                    if (Integer.TYPE.equals(cls)) {
                        method.invoke(o, 0);
                    } else if (Double.TYPE.equals(cls)) {
                        method.invoke(o, 0.0D);
                    } else if (Float.TYPE.equals(cls)) {
                        method.invoke(o, 0.0F);
                    } else if (Boolean.TYPE.equals(cls)) {
                        method.invoke(o, false);
                    } else if (Short.TYPE.equals(cls)) {
                        method.invoke(o, Short.valueOf((short) 0));
                    } else if (Long.TYPE.equals(cls)) {
                        method.invoke(o, 0L);
                    } else if (Character.TYPE.equals(cls)) {
                        method.invoke(o, '\u0000');
                    } else if (Byte.TYPE.equals(cls)) {
                        method.invoke(o, 0);
                    }

                    return;
                }
            }

            method.invoke(o, value);
        }
    }
}
