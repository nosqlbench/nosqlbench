package io.nosqlbench.virtdata.core.bindings;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class FunctionTyper {


    public static Class<?> getResultClass(Class<?> functionalType) {
        assertFunctionalInterface(functionalType);
        Method applyMethod = getMethod(functionalType);
        return applyMethod.getReturnType();
    }

    public static Class<?> getInputClass(Class<?> functionalType) {
        assertFunctionalInterface(functionalType);
        Method applyMethod = getMethod(functionalType);
        return applyMethod.getParameterTypes()[0];
    }

    public static Class<?> getArgType(Method applyMethod) {
        if (applyMethod.getParameterCount() != 1) {
            throw new RuntimeException(
                    "The parameter found is supposed to be 1, but it was" + applyMethod.getParameterCount()
            );
        }
        return applyMethod.getParameterTypes()[0];
    }

    private static Method getMethod(Class<?> functionalType) {
        assertFunctionalInterface(functionalType);
        Optional<Method> foundMethod = Arrays.stream(functionalType.getMethods())
                .filter(m -> !m.isSynthetic() && !m.isBridge() && !m.isDefault())
                .filter(m -> m.getName().startsWith("apply"))
                .findFirst();

        return foundMethod.orElseThrow(
                () -> new RuntimeException(
                        "Unable to find the function method on " + functionalType.getCanonicalName()
                )
        );
    }

    private static void assertFunctionalInterface(Class<?> functionalType) {
        if (functionalType.getAnnotation(FunctionalInterface.class)==null) {
            throw new RuntimeException("type " + functionalType.getCanonicalName() + " is not a functional type");
        }
        if (!functionalType.isInterface()) {
            throw new RuntimeException("type " + functionalType.getCanonicalName() + " is not an interface.");
        }
    }

}
