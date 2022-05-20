package io.nosqlbench.virtdata.core.composers;

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


import io.nosqlbench.virtdata.core.bindings.FunctionType;

import java.util.function.*;

public class ComposerForIntToDoubleFunction implements FunctionComposer<IntToDoubleFunction> {

    private final IntToDoubleFunction inner;

    public ComposerForIntToDoubleFunction(IntToDoubleFunction inner) {
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
        switch(functionType) {
            case long_long:
                final IntToLongFunction f1 =
                        (int i) ->
                                ((LongUnaryOperator)outer).applyAsLong((int) inner.applyAsDouble(i));
                return new ComposerForIntToLongFunction(f1);
            case long_T:
                final IntFunction<?> f2 =
                        (int i) ->
                                ((LongFunction<?>)outer).apply((int) inner.applyAsDouble(i));
                return new ComposerForIntFunction(f2);
            case long_int:
                final IntUnaryOperator f3 =
                        (int i) ->
                                ((LongToIntFunction)outer).applyAsInt((int) inner.applyAsDouble(i));
                return new ComposerForIntUnaryOperator(f3);
            case long_double:
                final IntToDoubleFunction f4 =
                        (int i) ->
                                ((LongToDoubleFunction)outer).applyAsDouble((int) inner.applyAsDouble(i));
                return new ComposerForIntToDoubleFunction(f4);
            case int_int:
                final IntUnaryOperator f5 =
                        (int i) ->
                                ((IntUnaryOperator)outer).applyAsInt((int) inner.applyAsDouble(i));
                return new ComposerForIntUnaryOperator(f5);
            case R_T:
                final IntFunction f6 =
                        (int i) ->
                                ((Function<Double,?>)outer).apply(inner.applyAsDouble(i));
                return new ComposerForIntFunction(f6);
            case int_long:
                final IntToLongFunction f7 =
                        (int i) ->
                                ((IntToLongFunction)outer).applyAsLong((int) inner.applyAsDouble(i));
                return new ComposerForIntToLongFunction(f7);
            case int_double:
                final IntToDoubleFunction f8 =
                        (int i) ->
                                ((IntToDoubleFunction)outer).applyAsDouble((int) inner.applyAsDouble(i));
                return new ComposerForIntToDoubleFunction(f8);
            case int_T:
                final IntFunction<?> f9 =
                        (int i) ->
                                ((IntFunction<?>)outer).apply((int) inner.applyAsDouble(i));
                return new ComposerForIntFunction(f9);
            case double_double:
                final IntToDoubleFunction f10 =
                        (int i) -> ((DoubleUnaryOperator)outer).applyAsDouble(inner.applyAsDouble(i));
                return new ComposerForIntToDoubleFunction(f10);
            case double_long:
                final IntToLongFunction f11 =
                        (int i) -> ((DoubleToLongFunction)outer).applyAsLong(inner.applyAsDouble(i));
                return new ComposerForIntToLongFunction(f11);
            case double_int:
                final IntUnaryOperator f12 =
                        (int i) -> ((DoubleToIntFunction)outer).applyAsInt(inner.applyAsDouble(i));
                return new ComposerForIntUnaryOperator(f12);
            case double_T:
                final IntFunction<?> f13 =
                        (int i) -> ((DoubleFunction<?>)outer).apply(inner.applyAsDouble(i));
                return new ComposerForIntFunction(f13);
            default:
                throw new RuntimeException(functionType + " is not recognized");

        }
    }
}
