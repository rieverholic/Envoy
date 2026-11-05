package dev.riever.envoy.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static Class<?> load(String name, ClassLoader loader) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("class " + name + " not found", e);
        }
    }

    public static MethodHandle method(Class<?> clazz, String name, Class<?>... params) {
        try {
            return spread(LOOKUP.unreflect(clazz.getMethod(name, params)));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("method " + clazz.getName() + "." + name + " not found", e);
        }
    }

    public static MethodHandle declaredMethod(Class<?> clazz, String name, Class<?>... params) {
        try {
            Method m = clazz.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return spread(LOOKUP.unreflect(m));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("method " + clazz.getName() + "." + name + " not found", e);
        }
    }

    public static MethodHandle fieldGetter(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return spread(LOOKUP.unreflectGetter(f));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("field " + clazz.getName() + "." + name + " not found", e);
        }
    }

    public static Object invoke(MethodHandle handle, Object... args) {
        try {
            return (Object) handle.invokeExact(args);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    public static Object get(Object obj, String fieldName) throws ReflectiveOperationException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static Object invoke(Object obj, String methodName) throws ReflectiveOperationException {
        Method method = obj.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(obj);
    }

    public static Object invoke(Object obj, String methodName, Class<?> argType, Object arg) throws ReflectiveOperationException {
        Method method = obj.getClass().getDeclaredMethod(methodName, argType);
        method.setAccessible(true);
        return method.invoke(obj, arg);
    }

    private static MethodHandle spread(MethodHandle h) {
        MethodType t = h.type();
        return h.asType(t.generic()).asSpreader(Object[].class, t.parameterCount());
    }
}
