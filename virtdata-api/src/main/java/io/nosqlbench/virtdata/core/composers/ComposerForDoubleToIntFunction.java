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

public class ComposerForDoubleToIntFunction implements FunctionComposer<DoubleToIntFunction> {
    private final DoubleToIntFunction inner;

    public ComposerForDoubleToIntFunction(DoubleToIntFunction inner) {
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
                        (double d) -> ((LongUnaryOperator) outer).applyAsLong((inner).applyAsInt(d));
                return new ComposerForDoubleToLongFunction(f1);
            case long_int:
                final DoubleToIntFunction f2 =
                        (double d) -> ((LongToIntFunction) outer).applyAsInt(( inner).applyAsInt(d));
                return new ComposerForDoubleToIntFunction(f2);
            case long_double:
                final DoubleUnaryOperator f3 =
                        (double d) -> ((LongToDoubleFunction)outer).applyAsDouble((inner).applyAsInt(d));
                return new ComposerForDoubleUnaryOperator(f3);
            case long_T:
                final DoubleFunction<?> f4 =
                        (double d) -> ((LongFunction<?>)outer).apply((inner).applyAsInt(d));
                return new ComposerForDoubleFunction(f4);
            case int_int:
                final DoubleToIntFunction f5 =
                        (double d) -> ((IntUnaryOperator)outer).applyAsInt((inner).applyAsInt(d));
                return new ComposerForDoubleToIntFunction(f5);
            case int_long:
                final DoubleToLongFunction f6 =
                        (double d) -> ((IntToLongFunction)outer).applyAsLong((inner).applyAsInt(d));
                return new ComposerForDoubleToLongFunction(f6);
            case int_double:
                final DoubleUnaryOperator f7 =
                        (double d) -> ((IntToDoubleFunction)outer).applyAsDouble((inner).applyAsInt(d));
                return new ComposerForDoubleUnaryOperator(f7);
            case int_T:
                final DoubleFunction<?> f8 =
                        (double d) -> ((IntFunction<?>)outer).apply((inner).applyAsInt(d));
                return new ComposerForDoubleFunction(f8);
            case double_double:
                final DoubleUnaryOperator f9 =
                        (double d) -> ((DoubleUnaryOperator) outer).applyAsDouble((inner).applyAsInt(d));
                return new ComposerForDoubleUnaryOperator(f9);
            case double_long:
                final DoubleToLongFunction f10 =
                        (double d) -> ((DoubleToLongFunction)outer).applyAsLong((inner).applyAsInt(d));
                return new ComposerForDoubleToLongFunction(f10);
            case double_int:
                final DoubleToIntFunction f11 =
                        (double d) -> ((DoubleToIntFunction)outer).applyAsInt((inner).applyAsInt(d));
                return new ComposerForDoubleToIntFunction(f11);
            case double_T:
                final DoubleFunction<?> f12 =
                        (double d) -> ((DoubleFunction<?>)outer).apply((inner).applyAsInt(d));
                return new ComposerForDoubleFunction(f12);
            case R_T:
                final DoubleFunction<?> f13 =
                        (double d) -> ((Function<Integer,?>)outer).apply((inner).applyAsInt(d));
                return new ComposerForDoubleFunction(f13);
            default:
                throw new RuntimeException("Unrecognized function type:" + functionType);

        }
    }
}
