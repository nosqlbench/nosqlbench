package io.nosqlbench.virtdata.api.composers;

import io.nosqlbench.virtdata.api.FunctionType;

import java.util.function.*;

public class ComposerForDoubleUnaryOperator implements FunctionComposer<DoubleUnaryOperator> {
    private final DoubleUnaryOperator inner;

    public ComposerForDoubleUnaryOperator(DoubleUnaryOperator inner) {
        this.inner = inner;
    }

    @Override
    public Object getFunctionObject() {
        return inner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FunctionComposer andThen(Object outer) {
        FunctionType functionType = FunctionType.valueOf(outer);
        switch (functionType) {
            case long_long:
                final DoubleToLongFunction f1 =
                        (double d) -> ((LongUnaryOperator)outer).applyAsLong((long)(inner).applyAsDouble(d));
                return new ComposerForDoubleToLongFunction(f1);
            case long_int:
                final DoubleToIntFunction f2 =
                        (double d) -> ((LongToIntFunction)outer).applyAsInt((int)(inner).applyAsDouble(d));
                return new ComposerForDoubleToIntFunction(f2);
            case long_double:
                final DoubleUnaryOperator f3 =
                        (double d) -> ((LongToDoubleFunction)outer).applyAsDouble((long)(inner).applyAsDouble(d));
                return new ComposerForDoubleUnaryOperator(f3);
            case long_T:
                final DoubleFunction<?> f4 =
                        (double d) -> ((LongFunction<?>)outer).apply((long)(inner).applyAsDouble(d));
                return new ComposerForDoubleFunction(f4);
            case int_int:
                final DoubleToIntFunction f5 =
                        (double d) -> ((IntUnaryOperator)outer).applyAsInt((int)(inner).applyAsDouble(d));
                return new ComposerForDoubleToIntFunction(f5);
            case int_long:
                final DoubleToLongFunction f6 =
                        (double d) -> ((IntToLongFunction)outer).applyAsLong((int)(inner).applyAsDouble(d));
                return new ComposerForDoubleToLongFunction(f6);
            case int_double:
                final DoubleUnaryOperator f7 =
                        (double d) -> ((IntToDoubleFunction)outer).applyAsDouble((int)inner.applyAsDouble(d));
                return new ComposerForDoubleUnaryOperator(f7);
            case int_T:
                final DoubleFunction<?> f8 =
                        (double d) -> ((IntFunction<?>)outer).apply((int)(inner).applyAsDouble(d));
                return new ComposerForDoubleFunction(f8);
            case double_double:
                final DoubleUnaryOperator f9 =
                        (double d) -> ((DoubleUnaryOperator)outer).applyAsDouble(inner.applyAsDouble(d));
                return new ComposerForDoubleUnaryOperator(f9);
            case double_long:
                final DoubleToLongFunction f10 =
                        (double d) -> ((DoubleToLongFunction)outer).applyAsLong(inner.applyAsDouble(d));
                return new ComposerForDoubleToLongFunction(f10);
            case double_int:
                final DoubleToIntFunction f11 =
                        (double d) -> ((DoubleToIntFunction)outer).applyAsInt(inner.applyAsDouble(d));
                return new ComposerForDoubleToIntFunction(f11);
            case double_T:
                final DoubleFunction<?> f12 =
                        (double d) -> ((DoubleFunction<?>)outer).apply(inner.applyAsDouble(d));
                return new ComposerForDoubleFunction(f12);
            case R_T:
                final DoubleFunction<?> f13 =
                        (double d) -> ((Function<Double,?>)outer).apply(inner.applyAsDouble(d));
                return new ComposerForDoubleFunction(f13);
            default:
                throw new RuntimeException(functionType + " is not recognized");

        }
    }
}
