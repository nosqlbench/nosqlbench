/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.scenarios.findmax;

public record FrameSample(
    Criterion criterion,
    int index,
    long startAt,
    long endAt,
    double startval,
    double endval,
    double calculated,
    DoubleMap vars
) {
    public FrameSample {
        vars.put(criterion.name(), calculated);
    }

    public double weightedValue() {
        if (Double.isNaN(criterion.weight())) {
            return 1.0d;
        } else {
            return calculated * criterion().weight();
        }
    }

    private double calculatedValue() {
        return switch (criterion.evaltype()) {
            case direct -> endval;
            case deltaT -> (endval - startval) / seconds();
            case remix -> criterion.remix().applyAsDouble(vars);
        };
    }

    private double seconds() {
        return ((double) (endAt - startAt)) / 1000d;
    }

    public static FrameSample init(Criterion criterion, int index, DoubleMap vars) {
        return new FrameSample(criterion, index, 0, 0, Double.NaN, Double.NaN, Double.NaN, vars);
    }

    public FrameSample start(long startTime) {
        criterion.frameStartCallback().run();
        double v = (criterion().evaltype() == EvalType.deltaT) ? criterion().supplier().getAsDouble() : Double.NaN;
        return new FrameSample(criterion, index, startTime, 0L, v, Double.NaN, Double.NaN, vars);
    }

    public FrameSample stop(long stopTime) {
        double v2 = (criterion().evaltype() != EvalType.remix) ? criterion().supplier().getAsDouble() : Double.NaN;
        FrameSample intermediate = new FrameSample(criterion, index, startAt, stopTime, startval, v2, Double.NaN, vars);
        FrameSample complete = intermediate.tally();
        return complete;
    }

    private FrameSample tally() {
        return new FrameSample(criterion, index, startAt, endAt, startval, endval, calculatedValue(), vars);
    }

    @Override
    public String toString() {
        return String.format(
            "%30s % 3d  derived=% 12.3f  weighted=% 12.5f%s",
//            "%30s %03d dt[%4.2fS] dV[% 10.3f] dC[% 10.3f] wV=%010.5f%s",
            criterion.name(),
            index,
//            seconds(),
//            deltaV(),
            calculated,
            weightedValue(),
            (Double.isNaN(criterion.weight()) ? " [NEUTRAL WEIGHT]" : "")
        );
    }

    private double deltaV() {
        return endval - startval;
    }
}
