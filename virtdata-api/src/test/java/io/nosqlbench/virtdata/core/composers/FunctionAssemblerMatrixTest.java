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

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.FunctionType;
import io.nosqlbench.virtdata.core.bindings.DataMapperFunctionMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

public class FunctionAssemblerMatrixTest {

    @Test
    public void testFullPrimitiveMatrix() {

        int iteration=0;

        List<Object> funcs = new ArrayList<>();
        for (FunctionType functionType : FunctionType.values()) {
            Object[] objects = genFunctions(functionType);
            funcs.addAll(Arrays.asList(objects));
        }

        long totalIterations = funcs.size() * funcs.size();

        for (Object f1 : funcs) {
//            if (ft1 == FunctionType.R_T
//                    || ft1 == FunctionType.long_T
//                    || ft1 == FunctionType.int_T
//                    || ft1 == FunctionType.double_T) {
//                continue;
//            }
            for (Object f2 : funcs) {
//                if (ft2 == FunctionType.R_T
//                        || ft2 == FunctionType.long_T
//                        || ft2 == FunctionType.int_T
//                        || ft2 == FunctionType.double_T) {
//                    continue;
//                }
                iteration++;
                double pctDone = 100.0 * ((double) iteration / totalIterations);

                String testingSignature = "testing: f1:" + f1 + ", f2:" + f2;
                System.out.format("%3d/%3d %s",iteration,totalIterations, testingSignature);
                FunctionComposer assy = new FunctionAssembly();

                assy = assy.andThen(f1);
                assy = assy.andThen(f2);

                DataMapper g = DataMapperFunctionMapper.map(assy.getResolvedFunction().getFunctionObject());
                Object o = g.get(1L);
                System.out.println(" out:" + o);

            }
        }
    }

    private Object[] genFunctions(FunctionType ftype) {
        switch (ftype) {
            case long_double:
                return new Object[]{ new F_long_double() };
            case long_int:
                return new Object[]{  new F_long_int() };
            case long_long:
                return new Object[]{  new F_long_long() };
            case long_T:
                return new Object[]{  new F_long_T_Object(), new F_long_T_DOUBLE(), new F_long_T_LONG(), new F_long_T_INT() };
            case int_int:
                return new Object[]{  new F_int_int() };
            case R_T:
                return new Object[]{  new F_R_T_Object(), new F_R_T_LONG(), new F_R_T_DOUBLE(), new F_R_T_INT() };
            case int_long:
                return new Object[]{  new F_int_long() };
            case int_double:
                return new Object[]{  new F_int_double() };
            case int_T:
                return new Object[]{  new F_int_T_Object(), new F_int_T_LONG(), new F_int_T_DOUBLE(), new F_int_T_INT() };
            case double_double:
                return new Object[]{  new F_double_double() };
            case double_int:
                return new Object[]{  new F_double_int() };
            case double_long:
                return new Object[]{  new F_double_long() };
            case double_T:
                return new Object[]{  new F_double_T_Object(), new F_double_T_LONG(), new F_double_T_DOUBLE(), new F_double_T_INT() };
            default:
                throw new RuntimeException("unrecognized function type: " + ftype);
        }

    }

    private static class SimpleNamer {
        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    private static class F_double_T_LONG extends SimpleNamer implements DoubleToLongFunction {
        @Override
        public long applyAsLong(double value) {
            return (long) value;
        }
    }

    private static class F_double_T_DOUBLE extends SimpleNamer implements DoubleUnaryOperator {
        @Override
        public double applyAsDouble(double operand) {
            return operand;
        }
    }

    private static class F_double_T_INT extends SimpleNamer implements DoubleToIntFunction {
        @Override
        public int applyAsInt(double value) {
            return (int) value;
        }
    }

    private static class F_double_T_Object extends SimpleNamer implements DoubleFunction<Object>{
        @Override
        public Object apply(double value) {
            return (long) value;
        }
    }

    private static class F_double_int extends SimpleNamer implements DoubleToIntFunction {
        @Override
        public int applyAsInt(double value) {
            return (int) value;
        }
    }

    private static class F_double_long extends SimpleNamer implements DoubleToLongFunction {
        @Override
        public long applyAsLong(double value) {
            return (long) value;
        }
    }
    private static class F_long_double extends SimpleNamer implements LongToDoubleFunction {
        @Override
        public double applyAsDouble(long value) {
            return (double) value;
        }
    }

    private static class F_long_int extends SimpleNamer implements LongToIntFunction {
        @Override
        public int applyAsInt(long value) {
            return (int) value;
        }
    }

    private static class F_long_long extends SimpleNamer implements LongUnaryOperator {
        @Override
        public long applyAsLong(long operand) {
            return operand;
        }
    }

    private static class F_double_double extends SimpleNamer implements DoubleUnaryOperator {
        @Override
        public double applyAsDouble(double operand) {
            return operand;
        }
    }

    private static class F_long_T_Object extends SimpleNamer implements LongFunction<Object> {
        @Override
        public Object apply(long value) {
            return value;
        }
    }

    private static class F_long_T_LONG extends SimpleNamer implements LongFunction<Long> {
        @Override
        public Long apply(long value) {
            return value;
        }
    }

    private static class F_long_T_INT extends SimpleNamer implements LongFunction<Integer> {
        @Override
        public Integer apply(long value) {
            return (int) value;
        }
    }

    private static class F_long_T_DOUBLE extends SimpleNamer implements LongFunction<Double> {
        @Override
        public Double apply(long value) {
            return (double) value;
        }
    }


    private static class F_R_T_Object extends SimpleNamer implements Function<Object, Object> {
        @Override
        public String apply(Object object) {
            return String.valueOf(object);
        }
    }

    private static class F_R_T_LONG extends SimpleNamer implements Function<Object, Long> {
        @Override
        public Long apply(Object object) {
            return Double.valueOf(String.valueOf(object)).longValue();
        }
    }

    private static class F_R_T_DOUBLE extends SimpleNamer implements Function<Object, Double> {
        @Override
        public Double apply(Object object) {
            return Double.valueOf(String.valueOf(object));
        }
    }

    private static class F_R_T_INT extends SimpleNamer implements Function<Object, Integer> {
        @Override
        public Integer apply(Object object) {
            return Double.valueOf(String.valueOf(object)).intValue();
        }
    }


    private static class F_int_int extends SimpleNamer implements IntUnaryOperator {
        @Override
        public int applyAsInt(int operand) {
            return operand;
        }
    }

    private static class F_int_long extends SimpleNamer implements IntToLongFunction {
        @Override
        public long applyAsLong(int value) {
            return value;
        }
    }

    private static class F_int_double extends SimpleNamer implements IntToDoubleFunction {
        @Override
        public double applyAsDouble(int value) {
            return value;
        }
    }

    private static class F_int_T_LONG extends SimpleNamer implements IntFunction<Long> {
        @Override
        public Long apply(int value) {
            return (long) value;
        }
    }

    private static class F_int_T_DOUBLE extends SimpleNamer implements IntFunction<Double> {
        @Override
        public Double apply(int value) {
            return (double) value;
        }
    }

    private static class F_int_T_INT extends SimpleNamer implements IntUnaryOperator {
        @Override
        public int applyAsInt(int operand) {
            return operand;
        }
    }
    private static class F_int_T_Object extends SimpleNamer implements IntFunction<Object> {
        @Override
        public Object apply(int value) {
            return (long) value;
        }
    }
}
