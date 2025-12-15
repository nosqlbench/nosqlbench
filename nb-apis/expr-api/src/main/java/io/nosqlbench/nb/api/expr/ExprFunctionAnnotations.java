package io.nosqlbench.nb.api.expr;

/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.nb.api.expr.ExprFunctionExample;
import io.nosqlbench.nb.api.expr.annotations.ExprExample;
import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Helper utilities for working with {@link ExprFunctionSpec}-annotated methods on expression providers.
 */
public final class ExprFunctionAnnotations {

    // Ensure deterministic ordering of annotated methods; reflection order varies by runtime
    private static final Comparator<Method> METHOD_COMPARATOR = Comparator
        .comparing(Method::getName)
        .thenComparing(m -> Arrays.toString(m.getParameterTypes()));

    private ExprFunctionAnnotations() {}

    public static void registerAnnotatedFunctions(ExprRuntimeContext context, Object provider) {
        Objects.requireNonNull(provider, "provider");
        for (Method method : sortedAnnotatedMethods(provider.getClass())) {
            ExprFunctionSpec spec = method.getAnnotation(ExprFunctionSpec.class);
            method.setAccessible(true);
            ExprFunctionMetadata metadata = metadataFor(spec, method, provider.getClass());
            ExprFunction function = adaptMethod(method, provider, context);
            context.registerFunction(metadata, function);
        }
    }

    static Collection<ExprFunctionMetadata> extractMetadata(Class<?> providerClass) {
        List<ExprFunctionMetadata> metadata = new ArrayList<>();
        for (Method method : sortedAnnotatedMethods(providerClass)) {
            ExprFunctionSpec spec = method.getAnnotation(ExprFunctionSpec.class);
            metadata.add(metadataFor(spec, method, providerClass));
        }
        return metadata;
    }

    private static List<Method> sortedAnnotatedMethods(Class<?> providerClass) {
        return Arrays.stream(providerClass.getDeclaredMethods())
            .filter(method -> method.getAnnotation(ExprFunctionSpec.class) != null)
            .sorted(METHOD_COMPARATOR)
            .toList();
    }

    private static ExprFunctionMetadata metadataFor(ExprFunctionSpec spec, Method method, Class<?> providerClass) {
        String name = spec.name().isBlank() ? method.getName() : spec.name();
        String providerName = providerClass.getSimpleName();
        List<ExprFunctionExample> examples = new ArrayList<>();
        for (ExprExample example : method.getAnnotationsByType(ExprExample.class)) {
            examples.add(ExprFunctionExample.fromAnnotation(example));
        }
        return new ExprFunctionMetadata(
            name,
            spec.synopsis().isBlank() ? name + "()" : spec.synopsis(),
            spec.description(),
            examples,
            providerName
        );
    }

    private static ExprFunction adaptMethod(Method method, Object target, ExprRuntimeContext context) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean takesContext = parameterTypes.length > 0 && ExprRuntimeContext.class.isAssignableFrom(parameterTypes[0]);

        return args -> invoke(method, target, context, takesContext, parameterTypes, args);
    }

    private static Object invoke(Method method, Object target, ExprRuntimeContext context, boolean takesContext, Class<?>[] parameterTypes, Object[] args) {
        Object[] safeArgs = args == null ? new Object[0] : args;
        int offset = takesContext ? 1 : 0;
        int paramCount = parameterTypes.length - offset;
        Object[] invocation = new Object[parameterTypes.length];
        if (takesContext) {
            invocation[0] = context;
        }

        try {
            if (paramCount == 0) {
                if (safeArgs.length != 0) {
                    throw new IllegalArgumentException("Expression function '" + method.getName() + "' does not accept arguments");
                }
            } else if (method.isVarArgs()) {
                int argIndex = 0;
                for (int i = offset; i < parameterTypes.length; i++) {
                    boolean isVarArg = (i == parameterTypes.length - 1);
                    if (isVarArg) {
                        Class<?> componentType = parameterTypes[i].getComponentType();
                        invocation[i] = buildVarArgArray(componentType, safeArgs, argIndex);
                        argIndex = safeArgs.length;
                    } else {
                        if (argIndex >= safeArgs.length) {
                            throw new IllegalArgumentException("Expression function '" + method.getName() + "' expected at least " + paramCount + " arguments, but received " + safeArgs.length);
                        }
                        invocation[i] = safeArgs[argIndex++];
                    }
                }
            } else if (paramCount == 1 && parameterTypes[offset].isArray() && parameterTypes[offset].getComponentType() == Object.class) {
                // method(Object[] args)
                invocation[offset] = safeArgs;
            } else if (paramCount == safeArgs.length) {
                int argIndex = 0;
                for (int i = offset; i < parameterTypes.length; i++) {
                    invocation[i] = safeArgs[argIndex++];
                }
            } else {
                throw new IllegalArgumentException("Expression function '" + method.getName() + "' expected " + paramCount + " arguments, but received " + safeArgs.length);
            }

            return method.invoke(target, invocation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error invoking expression function '" + method.getName() + "'", e.getCause() != null ? e.getCause() : e);
        }
    }

    private static Object buildVarArgArray(Class<?> componentType, Object[] args, int startIndex) {
        int length = Math.max(0, args.length - startIndex);
        Object array = java.lang.reflect.Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            java.lang.reflect.Array.set(array, i, args[startIndex + i]);
        }
        return array;
    }
}
