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
import io.nosqlbench.virtdata.core.bindings.ValueType;

import java.util.function.*;

public class ComposerForDoubleFunction implements FunctionComposer<DoubleFunction<?>> {

    private final DoubleFunction<?> inner;

    public ComposerForDoubleFunction(DoubleFunction<?> inner) {
        this.inner = inner;
    }

    @Override
    public Object getFunctionObject() {
        return inner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FunctionComposer andThen(Object outer) {
        FunctionType outerFunctionType = FunctionType.valueOf(outer);
        Object outv = this.inner.apply(1);
        ValueType itype = ValueType.valueOfAssignableClass(outv.getClass());

        switch (outerFunctionType) {

            case long_long:
                switch (itype) {
                    case LONG:
                        final DoubleToLongFunction f11 = (double d) ->
                                ((LongUnaryOperator) outer).applyAsLong(((DoubleToLongFunction) inner).applyAsLong(d));
                        return new ComposerForDoubleToLongFunction(f11);
                    case DOUBLE:
                        final DoubleToLongFunction f12 = (double d) ->
                                ((LongUnaryOperator) outer).applyAsLong((long) ((DoubleUnaryOperator) inner).applyAsDouble(d));
                        return new ComposerForDoubleToLongFunction(f12);
                    case INT:
                        final DoubleToLongFunction f13 = (double d) ->
                                ((LongUnaryOperator) outer).applyAsLong(((DoubleToIntFunction) inner).applyAsInt(d));
                        return new ComposerForDoubleToLongFunction(f13);
                    default:
                        final DoubleToLongFunction f14 = (double d) ->
                                ((LongUnaryOperator) outer).applyAsLong(Long.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleToLongFunction(f14);
                }
            case long_T:
                final DoubleFunction<?> f2 =
                        (double d) -> ((LongFunction<?>) outer).apply(((DoubleFunction<Long>) inner).apply(d));
                return new ComposerForDoubleFunction(f2);
            case long_int:
                final DoubleToIntFunction f3 =
                        (double d) -> ((LongToIntFunction) outer).applyAsInt(((DoubleFunction<Long>) inner).apply(d));
                return new ComposerForDoubleToIntFunction(f3);
            case long_double:
                final DoubleUnaryOperator f4 =
                        (double d) -> ((LongToDoubleFunction) outer).applyAsDouble(((DoubleFunction<Long>) inner).apply(d));
                return new ComposerForDoubleUnaryOperator(f4);
            case int_int:
                switch (itype) {
                    case LONG:
                        final DoubleToIntFunction f51 = (double d) ->
                                ((IntUnaryOperator) outer).applyAsInt(((DoubleFunction<Long>) inner).apply(d).intValue());
                        return new ComposerForDoubleToIntFunction(f51);
                    case DOUBLE:
                        final DoubleToIntFunction f52 = (double d) ->
                                ((IntUnaryOperator) outer).applyAsInt(((DoubleFunction<Double>) inner).apply(d).intValue());
                        return new ComposerForDoubleToIntFunction(f52);
                    case INT:
                        final DoubleToIntFunction f53 = (double d) ->
                                ((IntUnaryOperator) outer).applyAsInt(((DoubleFunction<Integer>) inner).apply(d));
                        return new ComposerForDoubleToIntFunction(f53);
                    default:
                        final DoubleToIntFunction f54 = (double d) ->
                                ((IntUnaryOperator) outer).applyAsInt(Integer.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleToIntFunction(f54);
                }
            case int_long:
                switch (itype) {
                    case LONG:
                        final DoubleToLongFunction f61 =
                                (double d) -> ((IntToLongFunction) outer).applyAsLong(((DoubleFunction<Long>) inner).apply(d).intValue());
                        return new ComposerForDoubleToLongFunction(f61);
                    case DOUBLE:
                        final DoubleToLongFunction f62 =
                                (double d) -> ((IntToLongFunction) outer).applyAsLong(((DoubleFunction<Double>) inner).apply(d).intValue());
                        return new ComposerForDoubleToLongFunction(f62);
                    case INT:
                        final DoubleToLongFunction f63 =
                                (double d) -> ((IntToLongFunction) outer).applyAsLong(((DoubleFunction<Integer>) inner).apply(d));
                        return new ComposerForDoubleToLongFunction(f63);
                    default:
                        final DoubleToLongFunction f64 =
                                (double d) -> ((IntToLongFunction) outer).applyAsLong(Integer.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleToLongFunction(f64);
                }
            case int_double:
                switch (itype) {
                    case LONG:
                        final DoubleUnaryOperator f71 =
                                (double d) -> ((IntToDoubleFunction) outer).applyAsDouble(((DoubleFunction<Long>) inner).apply(d).intValue());
                        return new ComposerForDoubleUnaryOperator(f71);
                    case DOUBLE:
                        final DoubleUnaryOperator f72 =
                                (double d) -> ((IntToDoubleFunction) outer).applyAsDouble(((DoubleFunction<Double>) inner).apply(d).intValue());
                        return new ComposerForDoubleUnaryOperator(f72);
                    case INT:
                        final DoubleUnaryOperator f73 =
                                (double d) -> ((IntToDoubleFunction) outer).applyAsDouble(((DoubleFunction<Integer>) inner).apply(d));
                        return new ComposerForDoubleUnaryOperator(f73);
                    default:
                        final DoubleUnaryOperator f74 =
                                (double d) -> ((IntToDoubleFunction) outer).applyAsDouble(Integer.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleUnaryOperator(f74);
                }
            case int_T:
                switch (itype) {
                    case LONG:
                        final DoubleFunction<?> f81 =
                                (double d) -> ((IntFunction<?>) outer).apply(((DoubleFunction<Long>) inner).apply(d).intValue());
                        return new ComposerForDoubleFunction(f81);
                    case DOUBLE:
                        final DoubleFunction<?> f82 =
                                (double d) -> ((IntFunction<?>) outer).apply(((DoubleFunction<Double>) inner).apply(d).intValue());
                        return new ComposerForDoubleFunction(f82);
                    case INT:
                        final DoubleFunction<?> f83 =
                                (double d) -> ((IntFunction<?>) outer).apply(((DoubleFunction<Integer>) inner).apply(d));
                        return new ComposerForDoubleFunction(f83);
                    default:
                        final DoubleFunction<?> f84 =
                                (double d) -> ((IntFunction<?>) outer).apply(Integer.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleFunction(f84);
                }
            case double_double:
                switch (itype) {
                    case LONG:
                        final DoubleUnaryOperator f91 =
                                (double d) -> ((DoubleUnaryOperator) outer).applyAsDouble(((DoubleFunction<Long>) inner).apply(d));
                        return new ComposerForDoubleUnaryOperator(f91);
                    case DOUBLE:
                        final DoubleUnaryOperator f92 =
                                (double d) -> ((DoubleUnaryOperator) outer).applyAsDouble(((DoubleFunction<Double>) inner).apply(d));
                        return new ComposerForDoubleUnaryOperator(f92);
                    case INT:
                        final DoubleUnaryOperator f93 =
                                (double d) -> ((DoubleUnaryOperator) outer).applyAsDouble(((DoubleFunction<Integer>) inner).apply(d));
                        return new ComposerForDoubleUnaryOperator(f93);
                    default:
                        final DoubleUnaryOperator f94 =
                                (double d) -> ((DoubleUnaryOperator) outer).applyAsDouble(Double.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleUnaryOperator(f94);
                }
            case double_long:
                final DoubleToLongFunction f10 =
                        (double d) -> ((DoubleToLongFunction) outer).applyAsLong(((DoubleFunction<Long>) inner).apply(d));
                return new ComposerForDoubleToLongFunction(f10);
            case double_int:
                switch (itype) {
                    case LONG:
                        final DoubleToIntFunction f111 =
                                (double d) -> ((DoubleToIntFunction) outer).applyAsInt(((DoubleFunction<Long>) inner).apply(d));
                        return new ComposerForDoubleToIntFunction(f111);
                    case DOUBLE:
                        final DoubleToIntFunction f112 =
                                (double d) -> ((DoubleToIntFunction) outer).applyAsInt(((DoubleFunction<Double>) inner).apply(d));
                        return new ComposerForDoubleToIntFunction(f112);
                    case INT:
                        final DoubleToIntFunction f113 =
                                (double d) -> ((DoubleToIntFunction) outer).applyAsInt(((DoubleFunction<Integer>) inner).apply(d));
                        return new ComposerForDoubleToIntFunction(f113);
                    default:
                        final DoubleToIntFunction f114 =
                                (double d) -> ((DoubleToIntFunction) outer).applyAsInt(Double.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleToIntFunction(f114);
                }
            case double_T:
                switch (itype) {
                    case LONG:
                        final DoubleFunction<?> f121 =
                                (double d) -> ((DoubleFunction<?>) outer).apply(((DoubleFunction<Long>) inner).apply(d));
                        return new ComposerForDoubleFunction(f121);
                    case DOUBLE:
                        final DoubleFunction<?> f122 =
                                (double d) -> ((DoubleFunction<?>) outer).apply(((DoubleFunction<Double>) inner).apply(d));
                        return new ComposerForDoubleFunction(f122);
                    case INT:
                        final DoubleFunction<?> f123 =
                                (double d) -> ((DoubleFunction<?>) outer).apply(((DoubleFunction<Integer>) inner).apply(d));
                        return new ComposerForDoubleFunction(f123);
                    default:
                        final DoubleFunction<?> f124 =
                                (double d) -> ((DoubleFunction<?>) outer).apply(Double.valueOf(((DoubleFunction<Object>) inner).apply(d).toString()));
                        return new ComposerForDoubleFunction(f124);
                }
            case R_T:
                switch (itype) {
                    case LONG:
                        final DoubleFunction<?> f131 =
                                (double d) -> ((Function<Object,Object>)outer).apply(((DoubleToLongFunction)inner).applyAsLong(d));
                        return new ComposerForDoubleFunction(f131);
                    case DOUBLE:
                        final DoubleFunction<?> f132 =
                                (double d) -> ((Function<Object,Object>)outer).apply(((DoubleUnaryOperator)inner).applyAsDouble(d));
                        return new ComposerForDoubleFunction(f132);
                    case INT:
                        final DoubleFunction<?> f133 =
                                (double d) -> ((Function<Object,Object>)outer).apply(((DoubleToIntFunction)inner).applyAsInt(d));
                        return new ComposerForDoubleFunction(f133);
                    default:
                        final DoubleFunction<?> f134 =
                                (double d) -> ((Function<Object,Object>)outer).apply(((DoubleFunction<Object>)inner).apply(d));
                        return new ComposerForDoubleFunction(f134);
                }
            default:
                throw new RuntimeException(outerFunctionType + " is not recognized");

        }
    }
}
