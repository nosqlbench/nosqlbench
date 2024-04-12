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

public class ComposerForLongToIntFunction implements FunctionComposer<LongToIntFunction> {

    private final LongToIntFunction inner;

    public ComposerForLongToIntFunction(LongToIntFunction inner) {
        this.inner = inner;
    }

    @Override
    public Object getFunctionObject() {
        return inner;
    }

    @Override
    public FunctionComposer andThen(Object outer) {
        FunctionType outerFunctionType = FunctionType.valueOf(outer);
        switch (outerFunctionType) {
            case long_long:
                final LongUnaryOperator f1 =
                        (long l) ->
                                ((LongUnaryOperator) outer).applyAsLong(inner.applyAsInt(l));
                return new ComposerForLongUnaryOperator(f1);
            case long_T:
                final LongFunction<?> f2 =
                        (long l) ->
                                ((LongFunction<?>) outer).apply(inner.applyAsInt(l));
                return new ComposerForLongFunction(f2);
            case long_int:
                final LongToIntFunction f3 =
                        (long l) ->
                                ((LongToIntFunction) outer).applyAsInt((inner.applyAsInt(l)));
                return new ComposerForLongToIntFunction(f3);
            case long_double:
                final LongToDoubleFunction f4 =
                        (long l) ->
                                ((LongToDoubleFunction) outer).applyAsDouble(inner.applyAsInt(l));
                return new ComposerForLongToDoubleFunction(f4);
            case R_T:
                final LongFunction<?> f5 =
                        (long l) ->
                                ((Function<Integer,?>) outer).apply(inner.applyAsInt(l));
                return new ComposerForLongFunction(f5);
            case int_int:
                final LongToIntFunction f6 =
                        (long l) ->
                                ((IntUnaryOperator) outer).applyAsInt(inner.applyAsInt(l));
                return new ComposerForLongToIntFunction(f6);
            case int_long:
                final LongUnaryOperator f7 =
                        (long l) ->
                                ((IntToLongFunction) outer).applyAsLong(inner.applyAsInt(l));
                return new ComposerForLongUnaryOperator(f7);
            case int_double:
                final LongToDoubleFunction f8 =
                        (long l) ->
                                ((IntToDoubleFunction) outer).applyAsDouble(inner.applyAsInt(l));
                return new ComposerForLongToDoubleFunction(f8);
            case int_T:
                final LongFunction<?> f9 =
                        (long l) ->
                                ((IntFunction<?>)outer).apply(inner.applyAsInt(l));
                return new ComposerForLongFunction(f9);
            case double_double:
                final LongToDoubleFunction f10 =
                        (long l) -> ((DoubleUnaryOperator)outer).applyAsDouble(inner.applyAsInt(l));
                return new ComposerForLongToDoubleFunction(f10);
            case double_long:
                final LongUnaryOperator f11 =
                        (long l) -> ((DoubleToLongFunction)outer).applyAsLong(inner.applyAsInt(l));
                return new ComposerForLongUnaryOperator(f11);
            case double_int:
                final LongToIntFunction f12 =
                        (long l) -> ((DoubleToIntFunction)outer).applyAsInt(inner.applyAsInt(l));
                return new ComposerForLongToIntFunction(f12);
            case double_T:
                final LongFunction<?> f13 =
                        (long l) -> ((DoubleFunction<?>)outer).apply(inner.applyAsInt(l));
                return new ComposerForLongFunction(f13);

            default:
                throw new RuntimeException(outerFunctionType + " is not recognized");

        }
    }

}
