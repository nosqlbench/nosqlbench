package io.nosqlbench.virtdata.core.composers;

import io.nosqlbench.virtdata.core.bindings.FunctionType;

import java.util.function.*;

public class ComposerForLongToDoubleFunction implements FunctionComposer<LongToDoubleFunction> {
    private final LongToDoubleFunction inner;

    public ComposerForLongToDoubleFunction(LongToDoubleFunction inner) {
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
                final LongUnaryOperator f1 =
                        (long l) -> ((LongUnaryOperator) outer).applyAsLong((long) inner.applyAsDouble(l));
                return new ComposerForLongUnaryOperator(f1);
            case long_T:
                final LongFunction<?> f2 =
                        (long l) -> ((LongFunction<?>) outer).apply((long) inner.applyAsDouble(l));
                return new ComposerForLongFunction(f2);
            case long_int:
                final LongToIntFunction f3 =
                        (long l) -> ((LongToIntFunction) outer).applyAsInt((int) inner.applyAsDouble(l));
                return new ComposerForLongToIntFunction(f3);
            case long_double:
                final LongToDoubleFunction f4 =
                        (long l) -> ((LongToDoubleFunction) outer).applyAsDouble((long) inner.applyAsDouble(l));
                return new ComposerForLongToDoubleFunction(f4);
            case R_T:
                final LongFunction<?> f5 =
                        (long l) -> ((Function<Double,?>) outer).apply(inner.applyAsDouble(l));
                return new ComposerForLongFunction(f5);
            case int_int:
                final LongToIntFunction f6 =
                        (long l) ->
                                ((IntUnaryOperator) outer).applyAsInt((int) inner.applyAsDouble(l));
                return new ComposerForLongToIntFunction(f6);
            case int_long:
                final LongUnaryOperator f7 =
                        (long l) -> ((IntToLongFunction) outer).applyAsLong((int) inner.applyAsDouble(l));
                return new ComposerForLongUnaryOperator(f7);

            case int_double:
                final LongToDoubleFunction f8 =
                        (long l) ->
                                ((IntToDoubleFunction) outer).applyAsDouble((int) inner.applyAsDouble(l));
                return new ComposerForLongToDoubleFunction(f8);
            case int_T:
                final LongFunction<?> f9 =
                        (long l) ->
                                ((IntFunction<?>) outer).apply((int) inner.applyAsDouble(l));
                return new ComposerForLongFunction(f9);
            case double_double:
                final LongToDoubleFunction f10 =
                        (long l) -> ((DoubleUnaryOperator)outer).applyAsDouble(inner.applyAsDouble(l));
                return new ComposerForLongToDoubleFunction(f10);
            case double_long:
                final LongUnaryOperator f11 =
                        (long l) -> ((DoubleToLongFunction)outer).applyAsLong(inner.applyAsDouble(l));
                return new ComposerForLongUnaryOperator(f11);
            case double_int:
                final LongToIntFunction f12 =
                        (long l) -> ((DoubleToIntFunction)outer).applyAsInt(inner.applyAsDouble(l));
                return new ComposerForLongToIntFunction(f12);
            case double_T:
                final LongFunction<?> f13 =
                        (long l) -> ((DoubleFunction<?>)outer).apply(inner.applyAsDouble(l));
                return new ComposerForLongFunction(f13);

            default:
                throw new RuntimeException(functionType + " is not recognized");
        }
    }

}
