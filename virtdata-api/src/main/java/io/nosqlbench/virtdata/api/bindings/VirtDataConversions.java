/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.api.bindings;

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.LongFunction;

public class VirtDataConversions {


    private enum FuncType {
        LongToDoubleFunction(java.util.function.LongToDoubleFunction.class, long.class, double.class),
        LongToIntFunction(java.util.function.LongToIntFunction.class, long.class, int.class),
        LongFunction(LongFunction.class, long.class, Object.class),
        LongUnaryOperator(java.util.function.LongUnaryOperator.class, long.class, long.class),
        IntFunction(java.util.function.IntFunction.class, int.class, Object.class),
        IntToDoubleFunction(java.util.function.IntToDoubleFunction.class, int.class, double.class),
        IntToLongFunction(java.util.function.IntToLongFunction.class, int.class, long.class),
        IntUnaryOperator(java.util.function.IntUnaryOperator.class, int.class, int.class),
        DoubleFunction(java.util.function.DoubleFunction.class, double.class, Object.class),
        DoubleUnaryOperator(java.util.function.DoubleUnaryOperator.class, double.class, double.class),
        DoubleToLongFunction(java.util.function.DoubleToLongFunction.class, double.class, long.class),
        DoubleToIntFunction(java.util.function.DoubleToIntFunction.class, double.class, int.class),
        Function(java.util.function.Function.class, Object.class, Object.class);

        private final Class<?> functionClazz;
        private final Class<?> inputClazz;
        private final Class<?> outputClazz;


        FuncType(Class<?> functionClazz, Class<?> inputClazz, Class<?> outputClazz) {
            this.functionClazz = functionClazz;
            this.inputClazz = inputClazz;
            this.outputClazz = outputClazz;
        }

        public static FuncType valueOf(Class<?> clazz) {
            for (FuncType value : FuncType.values()) {
                if (value.functionClazz.isAssignableFrom(clazz)) {
                    return value;
                }
            }
            throw new InvalidParameterException("No func type was found for " + clazz.getCanonicalName());
        }

    }

    private static final Logger logger = LogManager.getLogger(VirtDataConversions.class);

    public static <F,T> T[] adaptFunctionArray(F[] functionObjects, Class<T> functionType, Class<Object>... resultSignature) {
        T[] functions = (T[]) Array.newInstance(functionType, functionObjects.length);

        for (int i = 0; i < functionObjects.length; i++) {
            F func = functionObjects[i];
            T adapted = adaptFunction(func, functionType, resultSignature);
            functions[i]=adapted;
        }
        return functions;
    }


    public static <F, T> List<T> adaptFunctionList(F[] funcs, Class<T> functionType, Class<Object>... resultSignature) {
        List<T> functions = new ArrayList<>();
        for (Object func : funcs) {
            T adapted = adaptFunction(func, functionType, resultSignature);
            functions.add(adapted);
        }
        return functions;
    }

    /**
     * Adapt a functional object into a different type of functional object.
     *
     * @param func            The original function object.
     * @param functionType    The type of the function you need
     * @param <F>             The generic type of function being converted from
     * @param <T>             The generic type of function being converted to
     * @param resultSignature The signature of all output types, linearized for use after type-erasure.
     * @return An instance of T
     */
    public static <F, T> T adaptFunction(F func, Class<T> functionType, Class<?>... resultSignature) {

        FuncType funcType = FuncType.valueOf(func.getClass());

        List<Class<?>> signature = new ArrayList<>();
        List<Class<?>> fromSignature = linearizeObjectSignature(func);

        List<Class<?>> resultTypes = new ArrayList<>();
        resultTypes.add(functionType);
        Collections.addAll(resultTypes, resultSignature);
        List<Class<?>> toSignature = linearizeSignature(resultTypes);

        signature.addAll(fromSignature);
        signature.addAll(toSignature);

        logger.debug("Adapting function from " + fromSignature + " to " + toSignature);
        if (fromSignature.equals(toSignature)) {
            return (T) func;
        }
        if (isAssignableFromTo(fromSignature, toSignature)) {
            return (T) func;
        }

        Class<?>[] methodSignature = signature.toArray(new Class<?>[0]);

        Method adapter = null;
        Class<?> hostclass = NBFunctionConverter.class;
        try {
            logger.debug("Looking for adapter method for " + hostclass.getCanonicalName() + " with signature " + signature);
            adapter = NBFunctionConverter.class.getMethod("adapt", methodSignature);
        } catch (NoSuchMethodException e) {

            logger.debug("No adapter method found for " + hostclass.getCanonicalName() + " with signature " + signature);
            StringBuilder example = new StringBuilder();


            example.append("    // Ignore the place holders in your implementation, but ensure the return type is accurate\n");
            String toTypeSyntax = canonicalSyntaxFor(toSignature);
            example.append("    public static ").append(toTypeSyntax);

            example.append(" adapt(");
            String fromTypeSyntax = canonicalSyntaxFor(fromSignature);
            example.append(fromTypeSyntax).append(" f");
            int idx = 1;

            for (int i = 1; i < signature.size(); i++) {
                Class<?> sigpart = signature.get(i);
                example.append(", ").append(sigpart.getSimpleName()).append(" i").append(idx++);
            }

            example.append(") {\n    }\n");

            String forInstance = example.toString();
            throw new BasicError("adapter method is not implemented on class " + hostclass.getCanonicalName() + ":\n" + forInstance);

        }

        logger.debug("Found adapter method for " + hostclass.getCanonicalName() + " with signature " + signature);

        FuncType fromType = FuncType.valueOf(func.getClass());
        if (fromType.functionClazz.getTypeParameters().length > 0) {
            TypeVariable<? extends Class<?>>[] funcParms = func.getClass().getTypeParameters();
        }

        Object[] args = new Object[signature.size()];
        args[0] = func;
        for (int i = 1; i < args.length; i++) {
            args[i] = null;
        }

        T result = null;

        try {
            logger.debug("Invoking adapter method for " + hostclass.getCanonicalName() + " with signature "
                    + signature + " and args " + Arrays.toString(args));
            result = (T) adapter.invoke(null, args);
            return result;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Slice the incoming object list into a set of functions, based on a grouping interval and an offset.
     *
     * @param mod    The grouping interval, or modulo to slice the function groups into
     * @param offset The offset within the group for the provided function
     * @param funcs  A list of source objects to convert to functions.
     * @return
     */
    public static <T> List<T> getFunctions(int mod, int offset, Class<? extends T> functionType, Object... funcs) {
//        if ((funcs.length%mod)!=0) {
//            throw new RuntimeException("uneven division of functions, where multiples of " + mod + " are expected.");
//        }
        List<T> functions = new ArrayList<>();
        for (int i = offset; i < funcs.length; i += mod) {
            Object func = funcs[i];
            T longFunction = VirtDataConversions.adaptFunction(func, functionType, Object.class);
            functions.add(longFunction);
        }
        return functions;
    }

    private static boolean isAssignableFromTo(List<Class<?>> fromSignature, List<Class<?>> toSignature) {
        if (fromSignature.size() != toSignature.size()) {
            return false;
        }
        for (int i = 0; i < fromSignature.size(); i++) {
            Class<?> aClass0 = fromSignature.get(i);
            Class<?> aClass1 = toSignature.get(i);
            if (!aClass1.isAssignableFrom(aClass0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This constructs a type-erasure compatible signature of a tuple of types based on the caller's specified types.
     * More specifically, it takes a type A and a type B and reifies their syntactical types as a list of runtime
     * classes.
     * <p>
     * For example, {@code LongFunction<Integer>, Function<Integer,String>} would be converted to {@code
     * LongFunction.class, Integer.class, Function.class, Integer.class, String.class}
     * <p>
     * The types must be provided by the caller.
     * <p>
     * The purpose of this is to create an unambiguous signature to allow explicit method lookup which is the only
     * pair-wise association of the two provided types within some namespace.
     * <p>
     * This only works for types recognized by FuncType, as imposing this type system exposes the generic parameters
     * that are useful for adapting functions.
     *
     * @return An array of classes
     */
    private static List<Class<?>> linearizeSignature(Class<?>... types) {
        List<Class<?>> reified = new ArrayList<>();
        LinkedList<Class<?>> provided = new LinkedList<>(Arrays.asList(types));

        while (provided.size() > 0) {
            Class<?> mainType = provided.removeFirst();
            FuncType funcType = FuncType.valueOf(mainType);
            reified.add(funcType.functionClazz);
            for (TypeVariable<? extends Class<?>> typeParameter : funcType.functionClazz.getTypeParameters()) {
                if (provided.size() == 0) {
                    throw new RuntimeException("ran out of type parameters while qualifying generic parameter " + typeParameter.getName() + " for " + funcType.functionClazz);
                }
                Class<?> paramType = provided.remove();
                if (paramType.isPrimitive()) {
                    throw new RuntimeException("You must provide non primitive types for parameter positions here, not " + paramType.getCanonicalName());
                }
                reified.add(paramType);
            }
        }

        return reified;
    }

    private static List<Class<?>> linearizeSignature(List<Class<?>> types) {
        return linearizeSignature(types.toArray(new Class<?>[0]));
    }

    /**
     * Create a linearized list of classes to represent the provided instance.
     *
     * @param f An object which is represnted in the FuncTypes enum
     * @return A list of classes that uniquely describe the functional type signature of f, with generic parameters made
     * explicit.
     */
    private static List<Class<?>> linearizeObjectSignature(Object f) {
        LinkedList<Class<?>> linearized = new LinkedList<>();

        FuncType type = FuncType.valueOf(f.getClass());
        Class<?> functionClazz = type.functionClazz;
        linearized.add(functionClazz);

        TypeVariable<? extends Class<?>>[] typeParameters = functionClazz.getTypeParameters();

        Method applyMethod = findApplyMethod(functionClazz);
        switch (typeParameters.length) {
            case 2:
                linearized.add(applyMethod.getParameterTypes()[0]);
            case 1:
                linearized.add(applyMethod.getReturnType());
        }

        if (linearized.size() > 0 && linearized.peekLast().equals(Object.class)) {
            Object out = null;
            try {
                Class<?> input = applyMethod.getParameterTypes()[0];
                switch (input.getSimpleName()) {
                    case "int":
                    case "Integer":
                        out = applyMethod.invoke(f, 1);
                        break;
                    case "long":
                    case "Long":
                        out = applyMethod.invoke(f, 1L);
                        break;
                    case "double":
                    case "Double":
                        out = applyMethod.invoke(f, 1d);
                        break;
                    default:

                        out = Object.class;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            linearized.removeLast();
            linearized.addLast(out.getClass());
        }

        return linearized;
    }

    private static String canonicalSyntaxFor(List<Class<?>> elements) {
        return canonicalSyntaxFor(elements.toArray(new Class<?>[0]));
    }

    private static String canonicalSyntaxFor(Class<?>... elements) {
        StringBuilder sb = new StringBuilder();
        sb.append(elements[0].getSimpleName());
        if (elements.length > 1) {
            sb.append("<");
            for (int i = 1; i < elements.length; i++) {
                sb.append(elements[i].getSimpleName()).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(">");
        }
        return sb.toString();
    }

    private static Method findApplyMethod(Class<?> c) {
        Optional<Method> applyMethods = Arrays.stream(c.getMethods())
                .filter(m -> {
                    boolean isNotDefault = !m.isDefault();
                    boolean isNotBridge = !m.isBridge();
                    boolean isNotSynthetic = !m.isSynthetic();
                    boolean isPublic = (m.getModifiers() & Modifier.PUBLIC) > 0;
                    boolean isNotString = !m.getName().equals("toString");
                    boolean isApplyMethod = m.getName().startsWith("apply");
                    boolean isFunctional = isNotDefault && isNotBridge && isNotSynthetic && isPublic && isNotString && isApplyMethod;
                    return isFunctional;
                })
                .distinct()
                .findFirst();
        return applyMethods.orElseThrow(() -> new RuntimeException("Unable to find apply method on " + c.getCanonicalName()));
    }

    private static Method findMethod(Class<?> hostclass, Class<?> fromClass, Class<?> toClass, Class<?>... generics) {

        Class<?>[] argTypes = new Class[generics.length + 2];
        argTypes[0] = fromClass;
        argTypes[1] = toClass;
        System.arraycopy(generics, 0, argTypes, 2, generics.length);

        try {
            return hostclass.getMethod("adapt", argTypes);
        } catch (NoSuchMethodException e) {

            StringBuilder example = new StringBuilder();

            StringBuilder genericsBuffer = new StringBuilder();
            TypeVariable<? extends Class<?>>[] typeParameters = toClass.getTypeParameters();
            if (typeParameters.length > 0) {
                genericsBuffer.append("<");

                for (int i = 0; i < typeParameters.length; i++) {
                    if (generics.length < typeParameters.length) {
                        throw new RuntimeException("You must provide " + typeParameters.length + " generic parameter types for " + toClass.getCanonicalName());
                    }
                    genericsBuffer.append(generics[i].getSimpleName());
                    genericsBuffer.append(",");
                }
                genericsBuffer.setLength(genericsBuffer.length() - 1);
                genericsBuffer.append(">");
            }
            String genericSignature = genericsBuffer.toString();

            example.append("    // Ignore the place holders in your implementation, but ensure the return type is accurate\n");
            example.append("    public static ").append(toClass.getSimpleName());
            example.append(genericSignature);


            example.append(" adapt(");
            example.append(fromClass.getSimpleName()).append(" f, ");
            example.append(toClass.getSimpleName()).append(genericSignature).append(" ignore0");
            int idx = 1;
            for (Class<?> generic : generics) {
                example.append(", ").append(generic.getSimpleName()).append(" ignore").append(+idx);
            }

            example.append(") {\n    }\n");
            String forInstance = example.toString();

            throw new BasicError("adapter method is not implemented on class " + hostclass.getCanonicalName() + ":\n" + forInstance);
        }
    }

    /**
     * Given a base object and a wanted type to convert it to, assert that the type of the base object is assignable to
     * the wanted type. Further, if the wanted type is a generic type, assert that additional classes are assignable to
     * the generic type parameters. Thus, if you want to assign to a generic type from a non-generic type, you must
     * qualify the types of values that will be used in those generic parameter positions in declaration order.
     *
     * <p>This is useful for taking any object and a known type and reifying it as the known type so that it can be
     * then used idiomatically with normal type awareness. This scenario occurs when you are accepting an open type for
     * flexiblity but then need to narrow the type sufficiently for additional conversion in a type-safe way.</p>
     *
     * @param base     The object to be assigned to the wanted type
     * @param wantType The class type that the base object needs to be assignable to
     * @param clazzes  The types of values which will checked against generic type parameters of the wanted type
     * @param <T>      Generic parameter T for the wanted type
     * @return The original object casted to the wanted type after verification of parameter assignability
     */
    private static <T> T assertTypesAssignable(
            Object base,
            Class<T> wantType,
            Class<?>... clazzes) {

        if (!wantType.isAssignableFrom(base.getClass())) {
            throw new InvalidParameterException("Unable to assign " + wantType.getCanonicalName() + " from " +
                    base.getClass().getCanonicalName());
        }

        TypeVariable<? extends Class<?>>[] typeParameters = base.getClass().getTypeParameters();
        if (typeParameters.length > 0) {
            if (clazzes.length != typeParameters.length) {
                throw new InvalidParameterException(
                        "type parameter lengths are mismatched:" + clazzes.length + ", " + typeParameters.length
                );
            }
            for (int i = 0; i < clazzes.length; i++) {
                Class<?> from = clazzes[i];
                TypeVariable<? extends Class<?>> to = typeParameters[i];
                boolean assignableFrom = to.getGenericDeclaration().isAssignableFrom(from);
                if (!assignableFrom) {
                    throw new InvalidParameterException("Can not assign " + from.getCanonicalName() + " to " + to.getGenericDeclaration().getCanonicalName());
                }
            }

        }

        return (T) (base);
    }

}
