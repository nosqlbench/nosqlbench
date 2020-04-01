package io.nosqlbench.virtdata.api;

import io.nosqlbench.virtdata.api.FunctionType;
import io.nosqlbench.virtdata.api.DataMapper;

import java.util.function.*;

/**
 * <p>This class implements an obtuse way of avoiding autoboxing and M:N type
 * mapping complexity by way of doublish dispatch. It was preferred over a more
 * generalized reflection and annotation-based approach. If it gets too verbose,
 * (for some definition of "too"), then it may be refactored.</p>
 * <p>The primary goal of this approach is to allow for primitive-level
 * lambdas when function are composed together. This will allow for significant
 * performance gains when there are only a few steps in a composed function
 * which are non-primitive, which is the general case.</p>
 * <p>Composition should be supported between all primitive functions
 * for types listed in TypeMap, as well as generic functions, with generic
 * functions as the last resort.</p>
 */
@SuppressWarnings("unchecked")
public class DataMapperFunctionMapper {

    public static <T> DataMapper<T> map(Object function) {
        FunctionType functionType = FunctionType.valueOf(function);

        switch (functionType) {
            case long_double:
                return (DataMapper<T>) map((LongToDoubleFunction) function);
            case long_int:
                return (DataMapper<T>) map((LongToIntFunction) function);
            case long_long:
                return (DataMapper<T>) map((LongUnaryOperator) function);
            case long_T:
                return (DataMapper<T>) map((LongFunction) function);
            case R_T:
                return (DataMapper<T>) map((Function) function);
            case int_int:
                return (DataMapper<T>) map((IntUnaryOperator) function);
            case int_long:
                return (DataMapper<T>) map((IntToLongFunction) function);
            case int_double:
                return (DataMapper<T>) map((IntToDoubleFunction) function);
            case int_T:
                return (DataMapper<T>) map((IntFunction) function);
            case double_double:
                return (DataMapper<T>) map((DoubleUnaryOperator) function);
            case double_long:
                return (DataMapper<T>) map((DoubleToLongFunction) function);
            case double_int:
                return (DataMapper<T>) map((DoubleToIntFunction) function);
            case double_T:
                return (DataMapper<T>) map((DoubleFunction) function);
            default:
                throw new RuntimeException(
                        "Function object was not a recognized type for mapping to a data mapping lambda: "
                                + function.toString());
        }



    }

    public static <R> DataMapper<R> map(DoubleFunction<R> f) {
        return (long l) -> f.apply((double) l);
    }

    public static DataMapper<Integer> map(DoubleToIntFunction f) {
        return f::applyAsInt;
    }

    public static DataMapper<Long> map(DoubleToLongFunction f) {
        return f::applyAsLong;
    }

    public static DataMapper<Double> map(DoubleUnaryOperator f) {
        return f::applyAsDouble;
    }

    public static <R> DataMapper<R> map(IntFunction<R> f) {
        return (long l) -> f.apply((int) l);
    }

    public static DataMapper<Long> map(IntToDoubleFunction f) {
        return (long l) -> (long) f.applyAsDouble((int) l);
    }

    public static DataMapper<Long> map(IntToLongFunction f) {
        return (long l) -> f.applyAsLong((int) l);
    }

    public static DataMapper<Integer> map(IntUnaryOperator f) {
        return (long l) -> f.applyAsInt((int) l);
    }

    public static DataMapper<Double> map(LongToDoubleFunction f) {
        return f::applyAsDouble;
    }

    public static DataMapper<Integer> map(LongToIntFunction f) {
        return f::applyAsInt;
    }

    public static DataMapper<Long> map(LongUnaryOperator f) {
        return f::applyAsLong;
    }

    public static <R> DataMapper<R> map(LongFunction<R> f) {
        return f::apply;
    }

    public static <R> DataMapper<R> map(Function<Long, R> f) {
        return f::apply;
    }

}
