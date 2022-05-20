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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.function.*;

public class ComposerForLongFunction implements FunctionComposer<LongFunction<?>> {
    private final static Logger logger  = LogManager.getLogger(ComposerForLongFunction.class);
    private final LongFunction<?> inner;

    public ComposerForLongFunction(LongFunction<?> inner) {
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
        Object outv = this.inner.apply(1L);
        ValueType itype = ValueType.valueOfAssignableClass(outv.getClass());
        switch (functionType) {

            case long_long:
                switch (itype) {
                    case LONG:
                        final LongUnaryOperator f11 =
                                (long l) -> ((LongUnaryOperator) outer).applyAsLong(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongUnaryOperator(f11);
                    case DOUBLE:
                        final LongUnaryOperator f12 =
                                (long l) -> ((LongUnaryOperator) outer).applyAsLong(((LongFunction<Double>) inner).apply(l).longValue());
                        return new ComposerForLongUnaryOperator(f12);
                    case INT:
                        final LongUnaryOperator f13 =
                                (long l) -> ((LongUnaryOperator) outer).applyAsLong(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongUnaryOperator(f13);
                    default:
                        final LongUnaryOperator f14 =
                                (long l) -> ((LongUnaryOperator) outer).applyAsLong(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).longValue());
                        return new ComposerForLongUnaryOperator(f14);
                }
            case long_T:
                switch (itype) {
                    case LONG:
                        final LongFunction<?> f21 =
                                (long l) -> ((LongFunction<?>) outer).apply(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongFunction(f21);
                    case DOUBLE:
                        final LongFunction<?> f22 =
                                (long l) -> ((LongFunction<?>) outer).apply(((LongFunction<Double>) inner).apply(l).longValue());
                        return new ComposerForLongFunction(f22);
                    case INT:
                        final LongFunction<?> f23 =
                                (long l) -> ((LongFunction<?>) outer).apply(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongFunction(f23);
                    default:
                        final LongFunction<?> f24 =
                                (long l) -> ((LongFunction<?>) outer).apply(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).longValue());
                        return new ComposerForLongFunction(f24);
                }
            case long_int:
                switch (itype) {
                    case LONG:
                        final LongToIntFunction f31 =
                                (long l) -> ((LongToIntFunction) outer).applyAsInt(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongToIntFunction(f31);
                    case DOUBLE:
                        final LongToIntFunction f32 =
                                (long l) -> ((LongToIntFunction) outer).applyAsInt(((LongFunction<Double>) inner).apply(l).longValue());
                        return new ComposerForLongToIntFunction(f32);
                    case INT:
                        final LongToIntFunction f33 =
                                (long l) -> ((LongToIntFunction) outer).applyAsInt(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongToIntFunction(f33);
                    default:
                        final LongToIntFunction f34 =
                                (long l) -> ((LongToIntFunction) outer).applyAsInt(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).longValue());
                        return new ComposerForLongToIntFunction(f34);
                }
            case long_double:
                switch (itype) {
                    case LONG:
                        final LongToDoubleFunction f41 =
                                (long l) -> ((LongToDoubleFunction) outer).applyAsDouble(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongToDoubleFunction(f41);
                    case DOUBLE:
                        final LongToDoubleFunction f42 =
                                (long l) -> ((LongToDoubleFunction) outer).applyAsDouble(((LongFunction<Double>) inner).apply(l).longValue());
                        return new ComposerForLongToDoubleFunction(f42);
                    case INT:
                        final LongToDoubleFunction f43 =
                                (long l) -> ((LongToDoubleFunction) outer).applyAsDouble(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongToDoubleFunction(f43);
                    default:
                        final LongToDoubleFunction f44 =
                                (long l) -> ((LongToDoubleFunction) outer).applyAsDouble(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).longValue());
                        return new ComposerForLongToDoubleFunction(f44);
                }
            case R_T:
                final LongFunction<?> f5 =
                        (long l) -> ((Function<Object, Object>) outer).apply(((LongFunction<Object>) inner).apply(l));
                return new ComposerForLongFunction(f5);
            case int_int:
                switch (itype) {
                    case LONG:
                        final LongToIntFunction f61 = (long l) ->
                                ((IntUnaryOperator) outer).applyAsInt(((LongFunction<Long>) inner).apply(l).intValue());
                        return new ComposerForLongToIntFunction(f61);
                    case INT:
                        final LongToIntFunction f62 = (long l) ->
                                ((IntUnaryOperator) outer).applyAsInt(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongToIntFunction(f62);
                    case DOUBLE:
                        final LongToIntFunction f64 = (long l) ->
                                ((IntUnaryOperator) outer).applyAsInt(((LongFunction<Double>) inner).apply(l).intValue());
                        return new ComposerForLongToIntFunction(f64);
                    default:
                        final LongToIntFunction f63 = (long l) ->
                                ((IntUnaryOperator) outer).applyAsInt(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).intValue());
                        return new ComposerForLongToIntFunction(f63);
                }
            case int_long:
                switch (itype) {
                    case LONG:
                        final LongUnaryOperator f71 = (long l) ->
                                ((IntToLongFunction) outer).applyAsLong(((LongFunction<Long>) inner).apply(l).intValue());
                        return new ComposerForLongUnaryOperator(f71);
                    case INT:
                        final LongUnaryOperator f72 = (long l) ->
                                ((IntToLongFunction) outer).applyAsLong(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongUnaryOperator(f72);
                    case DOUBLE:
                        final LongUnaryOperator f73 = (long l) ->
                                ((IntToLongFunction) outer).applyAsLong(((LongFunction<Double>) inner).apply(l).intValue());
                        return new ComposerForLongUnaryOperator(f73);
                    default:
                        final LongUnaryOperator f74 = (long l) ->
                                ((IntToLongFunction) outer).applyAsLong(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).intValue());
                        return new ComposerForLongUnaryOperator(f74);
                }
            case int_double:
                switch (itype) {
                    case LONG:
                        final LongToDoubleFunction f81 =
                                (long l) -> ((IntToDoubleFunction) outer).applyAsDouble(((LongFunction<Long>) inner).apply(l).intValue());
                        return new ComposerForLongToDoubleFunction(f81);
                    case DOUBLE:
                        final LongToDoubleFunction f83 =
                                (long l) -> ((IntToDoubleFunction) outer).applyAsDouble(((LongFunction<Double>) inner).apply(l).intValue());
                        return new ComposerForLongToDoubleFunction(f83);
                    case INT:
                        final LongToDoubleFunction f82 =
                                (long l) -> ((IntToDoubleFunction) outer).applyAsDouble(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongToDoubleFunction(f82);
                    default:
                        final LongToDoubleFunction f84 =
                                (long l) -> ((IntToDoubleFunction) outer).applyAsDouble(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).intValue());
                        return new ComposerForLongToDoubleFunction(f84);
                }
            case int_T:
                switch (itype) {
                    case LONG:
                        final LongFunction<?> f91 =
                                (long l) -> ((IntFunction<Object>) outer).apply(((LongFunction<Long>) inner).apply(l).intValue());
                        return new ComposerForLongFunction(f91);
                    case DOUBLE:
                        final LongFunction<?> f92 =
                                (long l) -> ((IntFunction<Object>) outer).apply(((LongFunction<Double>) inner).apply(l).intValue());
                        return new ComposerForLongFunction(f92);
                    case INT:
                        final LongFunction<?> f93 =
                                (long l) -> ((IntFunction<Object>) outer).apply(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongFunction(f93);
                    default:
                        final LongFunction<?> f94 =
                                (long l) -> ((IntFunction<Object>) outer).apply(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()).intValue());
                        return new ComposerForLongFunction(f94);
                }
            case double_double:
                switch (itype) {
                    case LONG:
                        final LongToDoubleFunction f101 =
                                (long l) -> ((DoubleUnaryOperator) outer).applyAsDouble(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongToDoubleFunction(f101);
                    case DOUBLE:
                        final LongToDoubleFunction f102 =
                                (long l) -> ((DoubleUnaryOperator) outer).applyAsDouble(((LongFunction<Double>) inner).apply(l));
                        return new ComposerForLongToDoubleFunction(f102);
                    case INT:
                        final LongToDoubleFunction f103 =
                                (long l) -> ((DoubleUnaryOperator) outer).applyAsDouble(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongToDoubleFunction(f103);
                    default:
                        final LongToDoubleFunction f104 =
                                (long l) -> ((DoubleUnaryOperator) outer).applyAsDouble(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()));
                        return new ComposerForLongToDoubleFunction(f104);
                }
            case double_long:
                switch (itype) {
                    case LONG:
                        final LongUnaryOperator f111 =
                                (long l) -> ((DoubleToLongFunction) outer).applyAsLong(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongUnaryOperator(f111);
                    case INT:
                        final LongUnaryOperator f112 =
                                (long l) -> ((DoubleToLongFunction) outer).applyAsLong(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongUnaryOperator(f112);
                    case DOUBLE:
                        final LongUnaryOperator f113 =
                                (long l) -> ((DoubleToLongFunction) outer).applyAsLong(((LongFunction<Double>) inner).apply(l));
                        return new ComposerForLongUnaryOperator(f113);
                    default:
                        final LongUnaryOperator f114 =
                                (long l) -> ((DoubleToLongFunction) outer).applyAsLong(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()));
                        return new ComposerForLongUnaryOperator(f114);
                }
            case double_int:
                switch (itype) {
                    case LONG:
                        final LongToIntFunction f121 =
                                (long l) -> ((DoubleToIntFunction) outer).applyAsInt(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongToIntFunction(f121);
                    case INT:
                        final LongToIntFunction f122 =
                                (long l) -> ((DoubleToIntFunction) outer).applyAsInt(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongToIntFunction(f122);
                    case DOUBLE:
                        final LongToIntFunction f123 =
                                (long l) -> ((DoubleToIntFunction) outer).applyAsInt(((LongFunction<Double>) inner).apply(l));
                        return new ComposerForLongToIntFunction(f123);
                    default:
                        final LongToIntFunction f124 =
                                (long l) -> ((DoubleToIntFunction) outer).applyAsInt(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()));
                        return new ComposerForLongToIntFunction(f124);
                }

            case double_T:
                switch (itype) {
                    case LONG:
                        final LongFunction<Object> f131 =
                                (long l) -> ((DoubleFunction<Object>) outer).apply(((LongFunction<Long>) inner).apply(l));
                        return new ComposerForLongFunction(f131);
                    case DOUBLE:
                        final LongFunction<Object> f133 =
                                (long l) -> ((DoubleFunction<Object>) outer).apply(((LongFunction<Double>) inner).apply(l));
                        return new ComposerForLongFunction(f133);
                    case INT:
                        final LongFunction<Object> f132 =
                                (long l) -> ((DoubleFunction<Object>) outer).apply(((LongFunction<Integer>) inner).apply(l));
                        return new ComposerForLongFunction(f132);
                    default:
                        final LongFunction<Object> f134 =
                                (long l) -> ((DoubleFunction<Object>) outer).apply(Double.valueOf(((LongFunction<Object>) inner).apply(l).toString()));
                        return new ComposerForLongFunction(f134);
                }
            default:
                throw new RuntimeException(functionType + " is not recognized");

        }
    }
}
