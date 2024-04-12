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
import io.nosqlbench.virtdata.core.bindings.ValueType;

import java.util.function.*;

public class ComposerForIntFunction implements FunctionComposer<IntFunction<?>> {

    private final IntFunction<?> inner;

    public ComposerForIntFunction(IntFunction<?> inner) {
        this.inner = inner;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FunctionComposer<?> andThen(Object outer) {
        FunctionType functionType = FunctionType.valueOf(outer);
        Object outv = this.inner.apply(1);
        ValueType itype = ValueType.valueOfAssignableClass(outv.getClass());

        switch (functionType) {

            case long_long:
                switch (itype) {
                    case LONG:
                        final IntToLongFunction f11 = (int i) ->
                                ((LongUnaryOperator) outer).applyAsLong((((IntFunction<Long>) inner).apply(i)).intValue());
                        return new ComposerForIntToLongFunction(f11);
                    case DOUBLE:
                        final IntToLongFunction f12 = (int i) ->
                                ((LongUnaryOperator) outer).applyAsLong((((IntFunction<Double>) inner).apply(i)).intValue());
                        return new ComposerForIntToLongFunction(f12);
                    case INT:
                        final IntToLongFunction f13 = (int i) ->
                                ((LongUnaryOperator) outer).applyAsLong((((IntFunction<Integer>) inner).apply(i)));
                        return new ComposerForIntToLongFunction(f13);
                    default:
                        final IntToLongFunction f14 = (int i) ->
                                ((LongUnaryOperator) outer).applyAsLong(Double.valueOf((((IntFunction<Object>) inner).apply(i)).toString()).longValue());
                        return new ComposerForIntToLongFunction(f14);
                }
            case long_T:
                switch (itype) {
                    case LONG:
                        final IntFunction<?> f21 = (int i) ->
                                ((LongFunction<?>) outer).apply(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntFunction(f21);
                    case DOUBLE:
                        final IntFunction<?> f22 = (int i) ->
                                ((LongFunction<?>) outer).apply(((IntFunction<Double>) inner).apply(i).longValue());
                        return new ComposerForIntFunction(f22);
                    case INT:
                        final IntFunction<?> f23 = (int i) ->
                                ((LongFunction<?>) outer).apply(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntFunction(f23);
                    default:
                        final IntFunction<?> f24 = (int i) ->
                                ((LongFunction<?>) outer).apply(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()).longValue());
                        return new ComposerForIntFunction(f24);
                }
            case long_int:
                switch (itype) {
                    case LONG:
                        final IntUnaryOperator f31 = (int i) ->
                                ((LongToIntFunction) outer).applyAsInt(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntUnaryOperator(f31);
                    case DOUBLE:
                        final IntUnaryOperator f32 = (int i) ->
                                ((LongToIntFunction) outer).applyAsInt(((IntFunction<Double>) inner).apply(i).longValue());
                        return new ComposerForIntUnaryOperator(f32);
                    case INT:
                        final IntUnaryOperator f33 = (int i) ->
                                ((LongToIntFunction) outer).applyAsInt(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntUnaryOperator(f33);
                    default:
                        final IntUnaryOperator f34 = (int i) ->
                                ((LongToIntFunction) outer).applyAsInt(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()).longValue());
                        return new ComposerForIntUnaryOperator(f34);
                }
            case long_double:
                switch (itype) {
                    case LONG:
                        final IntToDoubleFunction f41 = (int i) ->
                                ((LongToDoubleFunction) outer).applyAsDouble(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntToDoubleFunction(f41);
                    case DOUBLE:
                        final IntToDoubleFunction f42 = (int i) ->
                                ((LongToDoubleFunction) outer).applyAsDouble(((IntFunction<Double>) inner).apply(i).longValue());
                        return new ComposerForIntToDoubleFunction(f42);
                    case INT:
                        final IntToDoubleFunction f43 = (int i) ->
                                ((LongToDoubleFunction) outer).applyAsDouble(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntToDoubleFunction(f43);
                    default:
                        final IntToDoubleFunction f44 = (int i) ->
                                ((LongToDoubleFunction) outer).applyAsDouble(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()).longValue());
                        return new ComposerForIntToDoubleFunction(f44);
                }
            case int_int:
                switch (itype) {
                    case LONG:
                        final IntUnaryOperator f23 = (int i) ->
                                ((IntUnaryOperator) outer).applyAsInt(((IntFunction<Long>) inner).apply(i).intValue());
                        return new ComposerForIntUnaryOperator(f23);
                    case DOUBLE:
                        final IntUnaryOperator f21 = (int i) ->
                                ((IntUnaryOperator) outer).applyAsInt(((IntFunction<Double>) inner).apply(i).intValue());
                        return new ComposerForIntUnaryOperator(f21);
                    case INT:
                        final IntUnaryOperator f22 = (int i) ->
                                ((IntUnaryOperator) outer).applyAsInt(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntUnaryOperator(f22);
                    default:
                        final IntUnaryOperator f24 = (int i) ->
                                ((IntUnaryOperator) outer).applyAsInt(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()).intValue());
                        return new ComposerForIntUnaryOperator(f24);
                }
            case R_T:
                switch (itype) {
                    case LONG:
                        final IntFunction<?> f61 = (int i) ->
                                ((Function<Object,Object>) outer).apply(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntFunction(f61);
                    case INT:
                        final IntFunction<?> f62 = (int i) ->
                                ((IntFunction<?>) outer).apply(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntFunction(f62);
                    case DOUBLE:
                        final IntFunction<?> f63 = (int i) ->
                                ((Function<Object,Object>) outer).apply(((IntFunction<Double>) inner).apply(i));
                        return new ComposerForIntFunction(f63);
                    default:
                        final IntFunction<?> f64 = (int i) ->
                                ((Function<Object,Object>) outer).apply(((IntFunction<Object>) inner).apply(i));
                        return new ComposerForIntFunction(f64);
                }
            case int_long:
                switch (itype)  {
                    case LONG:
                        final IntToLongFunction f71 = (int i) ->
                                ((IntToLongFunction) outer).applyAsLong(((IntFunction<Long>) inner).apply(i).intValue());
                        return new ComposerForIntToLongFunction(f71);
                    case INT:
                        final IntToLongFunction f72 = (int i) ->
                                ((IntToLongFunction) outer).applyAsLong(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntToLongFunction(f72);
                    case DOUBLE:
                        final IntToLongFunction f73 = (int i) ->
                                ((IntToLongFunction) outer).applyAsLong(((IntFunction<Long>) inner).apply(i).intValue());
                        return new ComposerForIntToLongFunction(f73);
                    default:
                        final IntToLongFunction f74 = (int i) ->
                                ((IntToLongFunction) outer).applyAsLong(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()).intValue());
                        return new ComposerForIntToLongFunction(f74);
                }
            case int_double:
                switch (itype) {
                    case LONG:
                        final IntToDoubleFunction f81 =
                                (int i) -> ((IntToDoubleFunction) outer).applyAsDouble(((IntFunction<Long>) inner).apply(i).intValue());
                        return new ComposerForIntToDoubleFunction(f81);
                    case INT:
                        final IntToDoubleFunction f82 =
                                (int i) -> ((IntToDoubleFunction) outer).applyAsDouble(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntToDoubleFunction(f82);
                    case DOUBLE:
                        final IntToDoubleFunction f83 =
                                (int i) -> ((IntToDoubleFunction) outer).applyAsDouble(((IntFunction<Long>) inner).apply(i).intValue());
                        return new ComposerForIntToDoubleFunction(f83);
                    default:
                        final IntToDoubleFunction f84 =
                                (int i) -> ((IntToDoubleFunction) outer).applyAsDouble(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()).intValue());
                        return new ComposerForIntToDoubleFunction(f84);
                }
            case int_T:
                switch (itype) {
                    case LONG:
                        final IntFunction<?> f91 =
                                (int i) -> ((IntFunction<?>) outer).apply(((IntFunction<Long>) inner).apply(i).intValue());
                        return new ComposerForIntFunction(f91);
                    case INT:
                        final IntFunction<?> f92 =
                                (int i) -> ((IntFunction<?>) outer).apply(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntFunction(f92);
                    case DOUBLE:
                        final IntFunction<?> f93 =
                                (int i) -> ((IntFunction<?>) outer).apply(((IntFunction<Double>) inner).apply(i).intValue());
                        return new ComposerForIntFunction(f93);
                    default:
                        final IntFunction<?> f94 =
                                (int i) -> ((IntFunction<?>) outer).apply(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()).intValue());
                        return new ComposerForIntFunction(f94);
                }
            case double_double:
                switch (itype) {
                    case LONG:
                        final IntToDoubleFunction f101 =
                                (int i) -> ((DoubleUnaryOperator) outer).applyAsDouble(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntToDoubleFunction(f101);
                    case DOUBLE:
                        final IntToDoubleFunction f102 =
                                (int i) -> ((DoubleUnaryOperator) outer).applyAsDouble(((IntFunction<Double>) inner).apply(i));
                        return new ComposerForIntToDoubleFunction(f102);
                    case INT:
                        final IntToDoubleFunction f103 =
                                (int i) -> ((DoubleUnaryOperator) outer).applyAsDouble(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntToDoubleFunction(f103);
                    default:
                        final IntToDoubleFunction f104 =
                                (int i) -> ((DoubleUnaryOperator) outer).applyAsDouble(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()));
                        return new ComposerForIntToDoubleFunction(f104);
                }
            case double_long:
                switch (itype) {
                    case LONG:
                        final IntToLongFunction f111 =
                                (int i) -> ((DoubleToLongFunction) outer).applyAsLong(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntToLongFunction(f111);
                    case DOUBLE:
                        final IntToLongFunction f112 =
                                (int i) -> ((DoubleToLongFunction) outer).applyAsLong(((IntFunction<Double>) inner).apply(i));
                        return new ComposerForIntToLongFunction(f112);
                    case INT:
                        final IntToLongFunction f113 =
                                (int i) -> ((DoubleToLongFunction) outer).applyAsLong(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntToLongFunction(f113);
                    default:
                        final IntToLongFunction f114 =
                                (int i) -> ((DoubleToLongFunction) outer).applyAsLong(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()));
                        return new ComposerForIntToLongFunction(f114);
                }
            case double_int:
                switch (itype) {
                    case LONG:
                        final IntUnaryOperator f121 =
                                (int i) -> ((DoubleToIntFunction) outer).applyAsInt(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntUnaryOperator(f121);
                    case DOUBLE:
                        final IntUnaryOperator f122 =
                                (int i) -> ((DoubleToIntFunction) outer).applyAsInt(((IntFunction<Double>) inner).apply(i));
                        return new ComposerForIntUnaryOperator(f122);
                    case INT:
                        final IntUnaryOperator f123 =
                                (int i) -> ((DoubleToIntFunction) outer).applyAsInt(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntUnaryOperator(f123);
                    default:
                        final IntUnaryOperator f124 =
                                (int i) -> ((DoubleToIntFunction) outer).applyAsInt(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()));
                        return new ComposerForIntUnaryOperator(f124);
                }

            case double_T:
                switch (itype) {
                    case LONG:
                        final IntFunction<?> f131 =
                                (int i) -> ((DoubleFunction<?>) outer).apply(((IntFunction<Long>) inner).apply(i));
                        return new ComposerForIntFunction(f131);
                    case DOUBLE:
                        final IntFunction<?> f132 =
                                (int i) -> ((DoubleFunction<?>) outer).apply(((IntFunction<Double>) inner).apply(i));
                        return new ComposerForIntFunction(f132);
                    case INT:
                        final IntFunction<?> f133 =
                                (int i) -> ((DoubleFunction<?>) outer).apply(((IntFunction<Integer>) inner).apply(i));
                        return new ComposerForIntFunction(f133);
                    default:
                        final IntFunction<?> f134 =
                                (int i) -> ((DoubleFunction<?>) outer).apply(Double.valueOf(((IntFunction<Object>) inner).apply(i).toString()));
                        return new ComposerForIntFunction(f134);
                }
            default:
                throw new RuntimeException(functionType + " is not recognized");

        }
    }

    @Override
    public Object getFunctionObject() {
        return inner;
    }

}
