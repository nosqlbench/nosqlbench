package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

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


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

@Categories({Category.conversion})
@ThreadSafeMapper
public class ToString implements DoubleFunction<String> {
    private final DoubleUnaryOperator doubleFunc;

    @Example({"ToString()","map the double input value to a String"})
    public ToString() {
        doubleFunc = (d) -> d;
    }

    @Example({"ToString(Add(5.7D))","map the double input value X to X+5.7D and then to a String"})
    public ToString(DoubleUnaryOperator df) {
        this.doubleFunc = df;
    }

    public ToString(DoubleFunction<Double> df) {
        this.doubleFunc = df::apply;
    }

    public ToString(Function<Double,Double> df) {
        this.doubleFunc = df::apply;
    }

    public String apply(double v) {
        return String.valueOf(doubleFunc.applyAsDouble(v));
    }
}
