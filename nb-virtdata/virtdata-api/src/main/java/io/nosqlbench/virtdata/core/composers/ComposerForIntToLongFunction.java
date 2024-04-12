/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.core.composers;

import io.nosqlbench.virtdata.core.bindings.FunctionType;

import java.util.function.*;

public class ComposerForIntToLongFunction implements FunctionComposer<IntToLongFunction> {
    private final IntToLongFunction inner;

    public ComposerForIntToLongFunction(IntToLongFunction inner) {
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
        switch(functionType) {

            case long_long:
                final IntToLongFunction f1 =
                        ((LongUnaryOperator) outer)::applyAsLong;
                return new ComposerForIntToLongFunction(f1);
            case long_T:
                final IntFunction<?> f2 =
                        (int i) ->
                                ((LongFunction<?>)outer).apply(inner.applyAsLong(i));
                return new ComposerForIntFunction(f2);
            case long_int:
                final IntUnaryOperator f3 =
                        (int i) ->
                                ((LongToIntFunction)outer).applyAsInt(inner.applyAsLong(i));
                return new ComposerForIntUnaryOperator(f3);
            case long_double:
                final IntToDoubleFunction f4 =
                        (int i) -> ((LongToDoubleFunction)outer).applyAsDouble(inner.applyAsLong(i));
                return new ComposerForIntToDoubleFunction(f4);
            case int_int:
                final IntUnaryOperator f5 =
                        (int i) ->
                                ((IntUnaryOperator)outer).applyAsInt((int) inner.applyAsLong(i));
                return new ComposerForIntUnaryOperator(f5);
            case R_T:
                final IntFunction<?> f6 =
                        (int i) ->
                                ((Function<Long,?>)outer).apply(inner.applyAsLong(i));
                return new ComposerForIntFunction(f6);
            case int_long:
                final IntToLongFunction f7 =
                        (int i) ->
                                ((IntToLongFunction)outer).applyAsLong((int) inner.applyAsLong(i));
                return new ComposerForIntToLongFunction(f7);
            case int_double:
                final IntToDoubleFunction f8 =
                        (int i) ->
                                ((IntToDoubleFunction)outer).applyAsDouble((int) inner.applyAsLong(i));
                return new ComposerForIntToDoubleFunction(f8);

            case int_T:
                final IntFunction<?> f9 =
                        (int i) ->
                                ((IntFunction<?>)outer).apply((int) inner.applyAsLong(i));
                return new ComposerForIntFunction(f9);
            case double_double:
                final IntToDoubleFunction f10 =
                        (int i) -> ((DoubleUnaryOperator)outer).applyAsDouble(inner.applyAsLong(i));
                return new ComposerForIntToDoubleFunction(f10);
            case double_long:
                final IntToLongFunction f11 =
                        (int i) -> ((DoubleToLongFunction)outer).applyAsLong(inner.applyAsLong(i));
                return new ComposerForIntToLongFunction(f11);
            case double_int:
                final IntUnaryOperator f12 =
                        (int i) -> ((DoubleToIntFunction)outer).applyAsInt(inner.applyAsLong(i));
                return new ComposerForIntUnaryOperator(f12);
            case double_T:
                final IntFunction<?> f13 =
                        (int i) -> ((DoubleFunction<?>)outer).apply(inner.applyAsLong(i));
                return new ComposerForIntFunction(f13);
            default:
                throw new RuntimeException(functionType + " is not recognized");

        }
    }
}
