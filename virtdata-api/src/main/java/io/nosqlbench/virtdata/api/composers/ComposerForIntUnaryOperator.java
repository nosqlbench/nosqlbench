package io.nosqlbench.virtdata.api.composers;

import io.nosqlbench.virtdata.api.FunctionType;

import java.util.function.*;

public class ComposerForIntUnaryOperator implements FunctionComposer<IntUnaryOperator> {

    private IntUnaryOperator inner;

    public ComposerForIntUnaryOperator(IntUnaryOperator inner) {
        this.inner = inner;
    }

    @Override
    public Object getFunctionObject() {
        return inner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FunctionComposer<?> andThen(Object outer) {
        FunctionType functionType = FunctionType.valueOf(outer);
        switch (functionType) {
            case long_long:
                final IntToLongFunction f1 =
                        (int i) ->
                                ((LongUnaryOperator) outer).applyAsLong(inner.applyAsInt(i));
                return new ComposerForIntToLongFunction(f1);
            case long_T:
                final IntFunction<?> f2 =
                        (int i) ->
                                ((LongFunction<?>) outer).apply(inner.applyAsInt(i));
                return new ComposerForIntFunction(f2);
            case long_int:
                final IntUnaryOperator f3 =
                        (int i) ->
                                ((LongToIntFunction) outer).applyAsInt(inner.applyAsInt(i));
                return new ComposerForIntUnaryOperator(f3);
            case long_double:
                final IntToDoubleFunction f4 =
                        (int i) ->
                                ((LongToDoubleFunction) outer).applyAsDouble(inner.applyAsInt(i));
                return new ComposerForIntToDoubleFunction(f4);
            case R_T:
                final IntFunction<?> f5 =
                        (int i) ->
                                ((Function<Integer, ?>) outer).apply(inner.applyAsInt(i));
                return new ComposerForIntFunction(f5);
            case int_int:
                final IntUnaryOperator f6 =
                        (int i) ->
                                ((IntUnaryOperator) outer).applyAsInt(inner.applyAsInt(i));
                return new ComposerForIntUnaryOperator(f6);

            case int_long:
                final IntToLongFunction f7 =
                        (int i) ->
                                ((IntToLongFunction) outer).applyAsLong(inner.applyAsInt(i));
                return new ComposerForIntToLongFunction(f7);
            case int_double:
                final IntToDoubleFunction f8 =
                        (int i) ->
                                ((IntToDoubleFunction) outer).applyAsDouble(inner.applyAsInt(i));
                return new ComposerForIntToDoubleFunction(f8);
            case int_T:
                final IntFunction<?> f9 =
                        (int i) ->
                                ((IntFunction<?>)outer).apply(inner.applyAsInt(i));
                return new ComposerForIntFunction(f9);

            case double_double:
                final IntToDoubleFunction f10 =
                        (int i) -> ((DoubleUnaryOperator)outer).applyAsDouble(inner.applyAsInt(i));
                return new ComposerForIntToDoubleFunction(f10);
            case double_long:
                final IntToLongFunction f11 =
                        (int i) -> ((DoubleToLongFunction)outer).applyAsLong(inner.applyAsInt(i));
                return new ComposerForIntToLongFunction(f11);
            case double_int:
                final IntUnaryOperator f12 =
                        (int i) -> ((DoubleToIntFunction)outer).applyAsInt(inner.applyAsInt(i));
                return new ComposerForIntUnaryOperator(f12);
            case double_T:
                final IntFunction<?> f13 =
                        (int i) -> ((DoubleFunction<?>)outer).apply(inner.applyAsInt(i));
                return new ComposerForIntFunction(f13);

            default:
                throw new RuntimeException(functionType + " is not recognized");

        }
    }

}
