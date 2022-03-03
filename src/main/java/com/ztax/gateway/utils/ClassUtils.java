package com.ztax.gateway.utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
    private static final List<String> pubColumnGetter = new ArrayList(16);
    private static final List<String> pubColumnSetter;

    private ClassUtils() {
    }

    public static List<Method> allGetters(Class<?> cls) {
        return allGetters(cls, false);
    }

    public static List<Method> nonPubColumnGetters(Class<?> cls) {
        return allGetters(cls, true);
    }

    public static List<Method> allSetters(Class<?> cls) {
        return allSetters(cls, false);
    }

    public static List<Method> nonPubColumnSetters(Class<?> cls) {
        return allSetters(cls, true);
    }

    public static String getterName(String fieldName, boolean isFieldClassBoolean) {
        if (isFieldClassBoolean) {
            return fieldName.startsWith("is") ? fieldName : "is" + firstCharUpperName(fieldName);
        } else {
            return "get" + firstCharUpperName(fieldName);
        }
    }

    public static String setterName(String fieldName, boolean isFieldClassBoolean) {
        return isFieldClassBoolean && fieldName.startsWith("is") ? "set" + fieldName.substring(2) : "set" + firstCharUpperName(fieldName);
    }

    public static Method getMethodByName(Class<?> cls, String methodName) {
        return getMethodByName(cls, methodName, -1);
    }

    public static Method getMethodByName(Class<?> cls, String methodName, int parameterCount) {
        if (methodName != null && !methodName.isEmpty() && cls != null) {
            List<Method> preAnswer = new ArrayList();
            Method[] var4 = cls.getMethods();
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Method method = var4[var6];
                if (methodName.equals(method.getName()) && (parameterCount == -1 || parameterCount == method.getParameterCount())) {
                    preAnswer.add(method);
                }
            }

            if (preAnswer.isEmpty()) {
                return null;
            } else if (preAnswer.size() > 1) {
                throw new RuntimeException("[" + methodName + "]存在多个重载方法");
            } else {
                return (Method) preAnswer.get(0);
            }
        } else {
            return null;
        }
    }

    public static String getFieldNameFromMethodName(String methodName) {
        if (!methodName.startsWith("set") && !methodName.startsWith("get")) {
            if (methodName.startsWith("is")) {
                return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
            } else {
                throw new RuntimeException("不支持从非getter/setter方法中取field名称");
            }
        } else {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
    }

    public static Class<?> genericActualClass(ParameterizedType type) {
        return genericActualClass((ParameterizedType) type, 0);
    }

    public static Class<?> genericActualClass(ParameterizedType type, int genericArgumentIndex) {
        return (Class) type.getActualTypeArguments()[genericArgumentIndex];
    }

    public static Class<?> genericActualClass(Class<?> cls) {
        return genericActualClass((Class) cls, 0);
    }

    public static Class<?> genericActualClass(Class<?> cls, int genericArgumentIndex) {
        Type type;
        try {
            type = cls.getGenericSuperclass();
        } catch (TypeNotPresentException var5) {
            return null;
        }

        if (!(type instanceof ParameterizedType)) {
            do {
                cls = cls.getSuperclass();

                try {
                    type = cls.getGenericSuperclass();
                } catch (TypeNotPresentException var4) {
                    return null;
                }
            } while (!(type instanceof ParameterizedType));
        }

        return genericActualClass((ParameterizedType) type, genericArgumentIndex);
    }

    private static String firstCharUpperName(String fieldName) {
        char c = fieldName.charAt(1);
        return c >= 'A' && c <= 'Z' ? fieldName : Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    private static List<Method> allGetters(Class<?> cls, boolean excludedPubColumn) {
        List<Method> answer = new ArrayList();
        Method[] var3 = cls.getMethods();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Method method = var3[var5];
            if (!"getClass".equals(method.getName()) && method.getParameterCount() <= 0 && (method.getName().startsWith("get") || method.getName().startsWith("is")) && (!excludedPubColumn || !pubColumnGetter.contains(method.getName()))) {
                answer.add(method);
            }
        }

        return answer;
    }

    private static List<Method> allSetters(Class<?> cls, boolean excludedPubColumn) {
        List<Method> answer = new ArrayList();
        Method[] var3 = cls.getMethods();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Method method = var3[var5];
            if (method.getParameterCount() == 1 && method.getName().startsWith("set") && (!excludedPubColumn || !pubColumnSetter.contains(method.getName()))) {
                answer.add(method);
            }
        }

        return answer;
    }

    static {
        pubColumnGetter.add("getPageNo");
        pubColumnGetter.add("getPageSize");
        pubColumnGetter.add("getOrderBy");
        pubColumnGetter.add("getNonEqualWhere");
        pubColumnGetter.add("getOrWhere");
        pubColumnGetter.add("getPubColumnWhere");
        pubColumnGetter.add("getConfiguredNonEqualWhere");
        pubColumnGetter.add("getConfiguredOrWhere");
        pubColumnGetter.add("getCreatorId");
        pubColumnGetter.add("getCreateTime");
        pubColumnGetter.add("getDataOrgId");
        pubColumnGetter.add("getUpdateUserId");
        pubColumnGetter.add("getUpdateTime");
        pubColumnGetter.add("getDeleteUserId");
        pubColumnGetter.add("getDeleteFlag");
        pubColumnGetter.add("getDeleteTime");
        pubColumnSetter = new ArrayList(16);
        pubColumnSetter.add("setPageNo");
        pubColumnSetter.add("setPageSize");
        pubColumnSetter.add("setOrderBy");
        pubColumnSetter.add("setNonEqualWhere");
        pubColumnSetter.add("setOrWhere");
        pubColumnSetter.add("setPubColumnWhere");
        pubColumnSetter.add("setCreatorId");
        pubColumnSetter.add("setCreateTime");
        pubColumnSetter.add("setDataOrgId");
        pubColumnSetter.add("setUpdateUserId");
        pubColumnSetter.add("setUpdateTime");
        pubColumnSetter.add("setDeleteUserId");
        pubColumnSetter.add("setDeleteFlag");
        pubColumnSetter.add("setDeleteTime");
    }
}
