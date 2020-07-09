package io.nosqlbench.virtdata.api.bindings;

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.TypeVariable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class VirtDataFunctions {

    private enum FuncType {
        LongToDoubleFunction(java.util.function.LongToDoubleFunction.class, double.class),
        LongToIntFunction(java.util.function.LongToIntFunction.class, int.class),
        LongFunction(LongFunction.class, long.class),
        LongUnaryOperator(java.util.function.LongUnaryOperator.class, long.class),
        IntFunction(java.util.function.IntFunction.class, int.class),
        IntUnaryOperator(java.util.function.IntUnaryOperator.class, int.class),
        DoubleFunction(java.util.function.DoubleFunction.class, double.class),
        DoubleUnaryOperator(java.util.function.DoubleUnaryOperator.class, double.class),
        Function(java.util.function.Function.class, Object.class);

        private final Class<?> functionClazz;
        private final Class<?> inputClazz;

        FuncType(Class functionClazz, Class<?> example) {
            this.functionClazz = functionClazz;
            this.inputClazz = example;
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

    /**
     * Adapt a functional object into a different type of functional object.
     *
     * @param func     The original function object.
     * @param type     The type of function object needed.
     * @param output   The output type required for the adapted function.
     * @param truncate Whether to throw an exception on any narrowing conversion. If this is set to false, then basic
     *                 roll-over logic is applied on narrowing conversions.
     * @param <F>      The type of function object needed.
     * @return An instance of F
     */
    public static <F> F adapt(Object func, Class<F> type, Class<?> output, boolean truncate) {
        FuncType funcType = FuncType.valueOf(type);
        switch (funcType) {
            case LongUnaryOperator:
                return truncate ? (F) adaptLongUnaryOperator(func, output) : (F) adaptLongUnaryOperatorStrict(func, output);
            case DoubleUnaryOperator:
                return truncate ? adaptDoubleUnaryOperator(func, output) : adaptDoubleUnaryOperatorStrict(func, output);
            case IntUnaryOperator:
                return truncate ? adaptIntUnaryOperator(func, output) : adaptIntUnaryOperatorStrict(func, output);
            case DoubleFunction:
                return truncate ? adaptDoubleFunction(func, output) : adaptDoubleFunctionStrict(func, output);
            case LongFunction:
                return truncate ? (F) adaptLongFunction(func, output) : (F) adaptLongFunctionStrict(func, output);
            case LongToDoubleFunction:
                return truncate ? (F) adaptLongToDoubleFunction(func, output) : (F) adaptLongToDoubleFunctionStrict(func, output);
            case LongToIntFunction:
                return truncate ? (F) adaptLongToIntFunction(func, output) : (F) adaptLongFunctionStrict(func, output);
            case IntFunction:
                return truncate ? adaptIntFunction(func, output) : adaptIntFunction(func, output);
            case Function:
                return truncate ? (F) adaptFunction(func, output) : adaptFunctionStrict(func, output);
            default:
                throw new RuntimeException("Unable to convert function type '" + funcType + "' (" + func.getClass().getCanonicalName() +
                        ") to " + type.getCanonicalName() + (truncate ? " WITH " : " WITHOUT ") + "truncation");

        }
    }


    public static <F extends Object> List<F> adaptList(Object[] funcs, Class<F> type, Class<?> output, boolean truncate) {
        List<F> adapted = new ArrayList<>();
        for (Object func : funcs) {
            F f = adapt(func, type, output, truncate);
            adapted.add(f);
        }
        return adapted;
    }

    private static LongToDoubleFunction adaptLongToDoubleFunctionStrict(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());
        switch (isaType) {
            case LongToDoubleFunction:
                LongToDoubleFunction f1 = assertTypesAssignable(func, LongToDoubleFunction.class);
                return f1::applyAsDouble;
            case LongToIntFunction:
                LongToIntFunction f2 = assertTypesAssignable(func, LongToIntFunction.class);
                return f2::applyAsInt;
            case LongFunction:
                LongFunction<Double> f3 = assertTypesAssignable(func, LongFunction.class, double.class);
                return f3::apply;
            case LongUnaryOperator:
                LongUnaryOperator f4 = assertTypesAssignable(func, LongUnaryOperator.class);
                return f4::applyAsLong;
            case DoubleFunction:
                DoubleFunction<Double> f7 = assertTypesAssignable(func, DoubleFunction.class, double.class);
                return f7::apply;
            case DoubleUnaryOperator:
                DoubleUnaryOperator f8 = assertTypesAssignable(func, DoubleUnaryOperator.class);
                return f8::applyAsDouble;
            case Function:
                Function<Double, Double> f9 = assertTypesAssignable(func, Function.class, double.class, double.class);
                return l -> f9.apply((double) l).doubleValue();
            case IntFunction:
            case IntUnaryOperator:
                throwNarrowingError(func, isaType.functionClazz);
            default:
                throw new BasicError("I don't know how to convert a " + func.getClass().getCanonicalName() + " function to a LongToDoubleFunction.");

        }
    }

    private static LongToDoubleFunction adaptLongToDoubleFunction(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());
        switch (isaType) {
            case LongToDoubleFunction:
                LongToDoubleFunction f1 = assertTypesAssignable(func, LongToDoubleFunction.class);
                return null;
            case LongToIntFunction:
                LongToIntFunction f2 = assertTypesAssignable(func, LongToIntFunction.class);
                return null;
            case LongFunction:
                LongFunction<Double> f3 = assertTypesAssignable(func, LongFunction.class, double.class);
                return null;
            case LongUnaryOperator:
                LongUnaryOperator f4 = assertTypesAssignable(func, LongUnaryOperator.class);
                return null;
            case IntFunction:
                IntFunction<Double> f5 = assertTypesAssignable(func, IntFunction.class, double.class);
                return null;
            case IntUnaryOperator:
                IntUnaryOperator f6 = assertTypesAssignable(func, IntUnaryOperator.class);
                return null;
            case DoubleFunction:
                DoubleFunction<Double> f7 = assertTypesAssignable(func, DoubleFunction.class, double.class);
                return null;
            case DoubleUnaryOperator:
                DoubleUnaryOperator f8 = assertTypesAssignable(func, DoubleUnaryOperator.class);
                return null;
            case Function:
                Function<Double, Double> f9 = assertTypesAssignable(func, Function.class, double.class, double.class);
                return null;

            default:
                throw new BasicError("I don't know how to convert a " + func.getClass().getCanonicalName() + " function to a LongToDoubleFunction.");

        }
    }

    private static LongFunction<?> adaptLongFunctionStrict(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());
        switch (isaType) {
            case LongFunction:
                LongFunction f1 = assertTypesAssignable(func, LongFunction.class, Object.class);
                return f1;
            case LongUnaryOperator:
                LongUnaryOperator f2 = assertTypesAssignable(func, LongUnaryOperator.class);
                return f2::applyAsLong;
            case LongToIntFunction:
                LongToIntFunction f3 = assertTypesAssignable(func, LongToIntFunction.class);
                return f3::applyAsInt;
            case Function:
                Function<Long, Long> f7 = assertTypesAssignable(func, Function.class, Long.class);
                return (long l) -> f7.apply(l);
            default:
                throw new RuntimeException("Unable to convert " + func.getClass().getCanonicalName() + " to a " +
                        LongUnaryOperator.class.getCanonicalName() + " since this would cause a narrowing conversion.");
        }

    }

    private static Function<?, ?> adaptFunction(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());
        switch (isaType) {
            case LongFunction:
                LongFunction<?> f1 = (LongFunction<?>) func;
                Function<Long, ?> rf1 = f1::apply;
                return rf1;
            case LongUnaryOperator:
                LongUnaryOperator f2 = (LongUnaryOperator) func;
                Function<Long, Long> rf2 = f2::applyAsLong;
                return rf2;
            case IntFunction:
                IntFunction f3 = (IntFunction) func;
                Function<Integer, ?> rf3 = f3::apply;
                return rf3;
            case IntUnaryOperator:
                IntUnaryOperator f4 = (IntUnaryOperator) func;
                Function<Integer, ?> rf4 = f4::applyAsInt;
                return rf4;
            case DoubleFunction:
                DoubleFunction f5 = (DoubleFunction) func;
                Function<Double, ?> rf5 = f5::apply;
                return rf5;
            case DoubleUnaryOperator:
                DoubleUnaryOperator f6 = (DoubleUnaryOperator) func;
                Function<Double, ?> rf6 = f6::applyAsDouble;
                return rf6;
            case LongToIntFunction:
                LongToIntFunction f7 = (LongToIntFunction) func;
                Function<Long, Integer> rf7 = f7::applyAsInt;
            case Function:
                return (Function<?, ?>) func;
            default:
                throw new RuntimeException("Unable to map function:" + func);
        }
    }

    private static <F> F adaptFunctionStrict(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    private static <F> F adaptDoubleFunctionStrict(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    private static <F> F adaptIntUnaryOperatorStrict(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    private static <F> F adaptDoubleUnaryOperatorStrict(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    private static LongUnaryOperator adaptLongUnaryOperatorStrict(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());

        switch (isaType) {
            case LongFunction:
                LongFunction<Long> o2 = assertTypesAssignable(func, LongFunction.class, long.class);
                return o2::apply;
            case LongUnaryOperator:
                LongUnaryOperator o5 = assertTypesAssignable(func, LongUnaryOperator.class);
                return o5;
            case Function:
                Function<Long, Long> o7 = assertTypesAssignable(func, Function.class, long.class, long.class);
                return o7::apply;
            default:
                throw new RuntimeException("Unable to convert " + func.getClass().getCanonicalName() + " to a " +
                        LongUnaryOperator.class.getCanonicalName() + " since this would cause a narrowing conversion.");
        }
    }

    private static <F> F adaptIntFunction(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    protected static LongToIntFunction adaptLongToIntFunction(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());

        switch (isaType) {
            case LongToDoubleFunction:
                LongToDoubleFunction f1 = assertTypesAssignable(func, LongToDoubleFunction.class, double.class);
                return l -> (int) (f1.applyAsDouble(l) % Integer.MAX_VALUE);
            case LongToIntFunction:
                LongToIntFunction f2 = assertTypesAssignable(func, LongToIntFunction.class);
                return f2;
            case LongFunction:
                LongFunction<Double> f3 = assertTypesAssignable(func, LongFunction.class, double.class);
                return l -> (int) f3.apply((int) l % Integer.MAX_VALUE).longValue();
            case LongUnaryOperator:
                LongUnaryOperator f4 = assertTypesAssignable(func, LongUnaryOperator.class);
                return l -> (int) (f4.applyAsLong(l) % Integer.MAX_VALUE);
            case IntFunction:
                IntFunction<Long> f5 = assertTypesAssignable(func, IntFunction.class, double.class);
                return l -> (int) f5.apply((int) l % Integer.MAX_VALUE).longValue() % Integer.MAX_VALUE;
            case IntUnaryOperator:
                IntUnaryOperator f6 = assertTypesAssignable(func, IntUnaryOperator.class);
                return l -> f6.applyAsInt((int) l % Integer.MAX_VALUE);
            case DoubleFunction:
                DoubleFunction<Double> f7 = assertTypesAssignable(func, DoubleFunction.class, double.class);
                return l -> (int) f7.apply(l).longValue() & Integer.MAX_VALUE;
            case DoubleUnaryOperator:
                DoubleUnaryOperator f8 = assertTypesAssignable(func, DoubleUnaryOperator.class);
                return l -> (int) f8.applyAsDouble(l) % Integer.MAX_VALUE;
            case Function:
                Function<Double, Double> f9 = assertTypesAssignable(func, Function.class, double.class, double.class);
                return l -> (int) f9.apply((double) l).longValue() % Integer.MAX_VALUE;
            default:
                throw new IllegalStateException("Unexpected value: " + isaType);
        }
    }


    private static LongFunction<?> adaptLongFunction(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());
        switch (isaType) {
            case LongFunction:
                LongFunction f1 = assertTypesAssignable(func, LongFunction.class, Object.class);
                return f1;
            case LongUnaryOperator:
                LongUnaryOperator f2 = assertTypesAssignable(func, LongUnaryOperator.class);
                return f2::applyAsLong;
            case IntFunction:
                IntFunction f3 = assertTypesAssignable(func, IntFunction.class);
                return (long l) -> f3.apply((int) (l % Integer.MAX_VALUE));
            case IntUnaryOperator:
                IntUnaryOperator f4 = assertTypesAssignable(func, IntUnaryOperator.class);
                return (long l) -> f4.applyAsInt((int) (l % Integer.MAX_VALUE));
            case DoubleFunction:
                DoubleFunction f5 = assertTypesAssignable(func, DoubleFunction.class);
                return f5::apply;
            case DoubleUnaryOperator:
                DoubleUnaryOperator f6 = assertTypesAssignable(func, DoubleUnaryOperator.class);
                return f6::applyAsDouble;
            case Function:
                Function<Long, Object> f7 = assertTypesAssignable(func, Function.class);
                assertOutputAssignable(f7.apply(1L), output);
                return (long l) -> f7.apply(l);
            case LongToDoubleFunction:
                LongToDoubleFunction f8 = assertTypesAssignable(func, LongToDoubleFunction.class);
                return f8::applyAsDouble;
            case LongToIntFunction:
                LongToIntFunction f9 = assertTypesAssignable(func, LongToIntFunction.class);
                assertOutputAssignable(f9.applyAsInt(1L), output);
                return (long l) -> f9.applyAsInt(l);
            default:
                throw new RuntimeException("Unable to convert " + func.getClass().getCanonicalName() + " to a " +
                        LongUnaryOperator.class.getCanonicalName());
        }
    }

    private static void assertOutputAssignable(Object result, Class<?> clazz) {
        if (!ClassUtils.isAssignable(result.getClass(), clazz, true)) {
            throw new InvalidParameterException("Unable to assign type of " + result.getClass().getCanonicalName()
                    + " to " + clazz.getCanonicalName());
        }

//        if (!clazz.isAssignableFrom(result.getClass())) {
//            throw new InvalidParameterException("Unable to assign type of " + result.getClass().getCanonicalName()
//                    + " to " + clazz.getCanonicalName());
//        }
    }

    private static <F> F adaptDoubleFunction(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    private static <F> F adaptIntUnaryOperator(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    private static <F> F adaptDoubleUnaryOperator(Object func, Class<?> output) {
        throw new RuntimeException("This must be implemented, now that it is used.");
    }

    private static LongUnaryOperator adaptLongUnaryOperator(Object func, Class<?> output) {
        FuncType isaType = FuncType.valueOf(func.getClass());

        switch (isaType) {
            case IntFunction:
                IntFunction<Long> o1 = assertTypesAssignable(func, IntFunction.class, long.class);
                return (long l) -> o1.apply((int) l % Integer.MAX_VALUE);
            case LongFunction:
                LongFunction<Long> o2 = assertTypesAssignable(func, LongFunction.class, long.class);
                return o2::apply;
            case DoubleFunction:
                DoubleFunction<Long> o3 = assertTypesAssignable(func, DoubleFunction.class, long.class);
                return o3::apply;
            case IntUnaryOperator:
                IntUnaryOperator o4 = assertTypesAssignable(func, IntUnaryOperator.class);
                return (long l) -> o4.applyAsInt((int) l % Integer.MAX_VALUE);
            case LongUnaryOperator:
                LongUnaryOperator o5 = assertTypesAssignable(func, LongUnaryOperator.class);
                return o5;
            case DoubleUnaryOperator:
                DoubleUnaryOperator o6 = assertTypesAssignable(func, DoubleUnaryOperator.class);
                return (long l) -> (long) (o6.applyAsDouble(l) % Long.MAX_VALUE);
            case Function:
                Function<Long, Long> o7 = assertTypesAssignable(func, Function.class, long.class, long.class);
                return o7::apply;
            case LongToDoubleFunction:
                LongToDoubleFunction o8 = assertTypesAssignable(func, LongToDoubleFunction.class);
                return l -> (long) o8.applyAsDouble(l % Long.MAX_VALUE) % Long.MAX_VALUE;
            case LongToIntFunction:
                LongToIntFunction o9 = assertTypesAssignable(func, LongToIntFunction.class);
                return o9::applyAsInt;
        }
        throw new InvalidParameterException("Unable to convert " + func.getClass().getCanonicalName() + " to a " +
                LongUnaryOperator.class.getCanonicalName());
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

    /**
     * Throw an error indicating a narrowing conversion was attempted for strict conversion.
     * @param func The source function to convert from
     * @param targetClass The target class which was requested
     */
    private static void throwNarrowingError(Object func, Class<?> targetClass) {
        throw new BasicError("Converting from " + func.getClass().getCanonicalName() + " to " + targetClass.getCanonicalName() +
                " is not allowed when strict conversion is requested.");
    }


}
