package ludo.mentis.aciem.mdc.util;

import java.lang.reflect.*;
import java.util.*;

public class DtoTester {

    public static <T> void testDto(Class<T> dtoClass) {
        try {
            var constructor = dtoClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            var instance1 = constructor.newInstance();
            var instance2 = constructor.newInstance();

            Map<String, Method> getters = new HashMap<>();
            Map<String, Method> setters = new HashMap<>();

            for (var method : dtoClass.getMethods()) {
                if (isGetter(method)) {
                    var property = getPropertyName(method.getName());
                    getters.put(property, method);
                } else if (isSetter(method)) {
                    var property = getPropertyName(method.getName());
                    setters.put(property, method);
                }
            }

            for (var property : setters.keySet()) {
                var setter = setters.get(property);
                var getter = getters.get(property);
                if (getter == null) continue;

                var paramType = setter.getParameterTypes()[0];
                var dummyValue = generateDummyValue(paramType);
                setter.invoke(instance1, dummyValue);
                setter.invoke(instance2, dummyValue);

                var result = getter.invoke(instance1);
                assert Objects.equals(result, dummyValue) : "Getter/setter mismatch for " + property;
            }

            assert instance1.equals(instance2) : "equals() not working as expected";
            assert instance1.hashCode() == instance2.hashCode() : "hashCode() not working as expected";

        } catch (Exception e) {
            throw new RuntimeException("DTO test failed for class: " + dtoClass.getName(), e);
        }
    }

    private static boolean isGetter(Method m) {
        return Modifier.isPublic(m.getModifiers())
                && m.getParameterCount() == 0
                && m.getName().startsWith("get")
                && !m.getReturnType().equals(void.class);
    }

    private static boolean isSetter(Method m) {
        return Modifier.isPublic(m.getModifiers())
                && m.getParameterCount() == 1
                && m.getName().startsWith("set");
    }

    private static String getPropertyName(String methodName) {
        return methodName.substring(3); // remove 'get' or 'set'
    }

    private static Object generateDummyValue(Class<?> type) {
        if (type.equals(String.class)) return "test";
        if (type.equals(int.class) || type.equals(Integer.class)) return 42;
        if (type.equals(long.class) || type.equals(Long.class)) return 42L;
        if (type.equals(double.class) || type.equals(Double.class)) return 42.0;
        if (type.equals(boolean.class) || type.equals(Boolean.class)) return true;
        if (type.equals(List.class)) return List.of("A", "B");
        if (type.equals(Set.class)) return Set.of("X", "Y");
        if (type.isEnum()) return type.getEnumConstants()[0];
        if (!type.isPrimitive()) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) {
                // ignored
            }
        }
        return null;
    }
}
