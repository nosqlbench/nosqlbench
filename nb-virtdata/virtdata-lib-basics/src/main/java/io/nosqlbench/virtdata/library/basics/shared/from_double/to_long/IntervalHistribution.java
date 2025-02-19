package io.nosqlbench.virtdata.library.basics.shared.from_double.to_long;

/*
 * Copyright (c) nosqlbench
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
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleLong;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbLongDouble;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.EmpiricalDistribution;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashRange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleToLongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
@Categories(Category.distributions)
public class IntervalHistribution implements LongUnaryOperator {

    private final UnitHistribution sampler;
    private final LongToDoubleFunction samplePointF;

    @Example({"IntervalHistribution('50 25 13 12')", "implied frequencies of 0:50 1:25 2:13 3:12"})
    @Example({
        "IntervalHistribution('234:50 33:25 17:13 3:12')",
        "labeled frequencies; 234,33,17,3 are labels, and 50,25,13,12 are weights"
    })
    public IntervalHistribution(String freqs, Object samplePointFunc) {
        this.sampler = new UnitHistribution(freqs);
        this.samplePointF = VirtDataFunctions.adapt(
            samplePointFunc,
            LongToDoubleFunction.class,
            double.class,
            false
        );
    }

    public IntervalHistribution(String freqs) {
        this(freqs,new HashRange(0.0d,1.0d));
    }



    private static List<EvProbLongDouble> genEvents(long[] freqs) {
        ArrayList<EvProbLongDouble> events = new ArrayList<>();
        for (int i = 0; i < freqs.length; i++) {
            events.add(new EvProbLongDouble(i, freqs[i]));
        }
        return events;
    }

    @Override
    public long applyAsLong(long operand) {
        double samplePoint = this.samplePointF.applyAsDouble(operand);
        return this.sampler.applyAsLong(samplePoint);
    }
}
