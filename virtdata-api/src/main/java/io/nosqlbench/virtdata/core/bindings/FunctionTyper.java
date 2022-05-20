package io.nosqlbench.virtdata.core.bindings;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
