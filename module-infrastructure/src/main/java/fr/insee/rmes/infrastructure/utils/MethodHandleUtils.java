package fr.insee.rmes.infrastructure.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class MethodHandleUtils {

    private static final MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
    private static final Map<MethodHandleEntry, Optional<MethodHandle>> methodHandleCache = new HashMap<>();

    private MethodHandleUtils() {
    }

    public static Optional<MethodHandle> findMethodHandle(Class<?> containerClass, String methodName, Class<?> returnedType, Class<?>... parametersTypes) {
        return methodHandleCache.computeIfAbsent(
                new MethodHandleEntry(containerClass, methodName, MethodType.methodType(returnedType, parametersTypes)),
                MethodHandleUtils::createMethodHandle);

    }

    private static Optional<MethodHandle> createMethodHandle(MethodHandleEntry methodHandleEntry) {
        try {
            return Optional.ofNullable(publicLookup.findVirtual(methodHandleEntry.containerClass(), methodHandleEntry.methodName(), methodHandleEntry.methodType()));
        } catch (NoSuchMethodException | IllegalAccessException | SecurityException | NullPointerException e) {
            return Optional.empty();
        }
    }

    public static Object safeInvokeMethodHandle(MethodHandle methodHandle, Object target, Object... parameters) {
        Object[] targetThenParameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, targetThenParameters, 1, parameters.length);
        targetThenParameters[0] = target;
        try {
            return requireNonNull(methodHandle).invokeWithArguments(targetThenParameters);
        } catch (Throwable e) {
            return null;
        }
    }

    record MethodHandleEntry(Class<?> containerClass, String methodName, MethodType methodType) {
    }

}
