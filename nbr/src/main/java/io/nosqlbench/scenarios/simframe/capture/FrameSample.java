/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.scenarios.simframe.capture;

import java.util.Objects;

/**
 * A frame sample is responsible for capturing the data associated with a single criterion as a single dependent
 * variable.
 */
public final class FrameSample {
    private final Criterion criterion;
    private final int index;
    private long startAt, endAt;
    private double startval = Double.NaN;
    private double endval = Double.NaN;
    private double basis = Double.NaN;
    private final BasisValues vars;
    private boolean active = false;

    public FrameSample(Criterion criterion, int index, BasisValues vars) {
        this.criterion = criterion;
        this.index = index;
        this.vars = vars;
    }

    public double weightedValue() {
        if (active) {
            calculateBasis();
        }
        double result = (Double.isNaN(criterion().weight()) ? 1.0d : (criterion().weight()) * basis);
        return result;
    }

    public FrameSample start(long startTime) {
        criterion.frameStartCallback().run();
        this.startAt = startTime;
        this.startval = (criterion().evaltype() == EvalType.deltaT) ? criterion().supplier().getAsDouble() : Double.NaN;
        active = true;
        return this;
    }

    public FrameSample stop(long endTime) {
        if (active) {
            this.endAt = endTime;
            this.endval = (criterion().evaltype() != EvalType.remix) ? criterion().supplier().getAsDouble() : Double.NaN;
            calculateBasis(endTime);
            this.active = false;
        } else {
            throw new RuntimeException("Can't stop an inactive frame.");
        }
        return this;
    }

    private void calculateBasis() {
        calculateBasis(System.currentTimeMillis());
    }

    private void calculateBasis(long now) {
        if (!active) {
            throw new RuntimeException("Calculations on inactive windows should not be done.");
        }
        this.endAt = now;
        this.endval = (criterion().evaltype() != EvalType.remix) ? criterion().supplier().getAsDouble() : Double.NaN;
        double seconds = deltaT();
        double basis = switch (criterion.evaltype()) {
            case direct -> endval;
            case deltaT -> deltaV() / seconds;
            case remix -> criterion.remix().applyAsDouble(vars);
        };
        vars.put(criterion().name(), basis);
        this.basis = basis;
    }

    private double deltaV() {
        return (endval - startval);
    }

    private double deltaT() {
        return ((double) (endAt - startAt)) / 1000d;
    }



    public Criterion criterion() {
        return criterion;
    }

    public int index() {
        return index;
    }

    public long startAt() {
        return startAt;
    }

    public long endAt() {
        return endAt;
    }

    public double startval() {
        return startval;
    }

    public double endval() {
        return endval;
    }

    public double basis() {
        return basis;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FrameSample) obj;
        return Objects.equals(this.criterion, that.criterion) &&
            this.index == that.index &&
            this.startAt == that.startAt &&
            this.endAt == that.endAt &&
            Double.doubleToLongBits(this.startval) == Double.doubleToLongBits(that.startval) &&
            Double.doubleToLongBits(this.endval) == Double.doubleToLongBits(that.endval) &&
            Double.doubleToLongBits(this.basis) == Double.doubleToLongBits(that.basis) &&
            Objects.equals(this.vars, that.vars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criterion, index, startAt, endAt, startval, endval, basis, vars);
    }

    @Override
    public String toString() {
        return switch (criterion().evaltype()) {
            case deltaT ->
                String.format(
                    "% 5d %30s  basis=% 12.3f ⋅ W=%1.1f C=% 17.5f   ΔV %12.5f  ΔT %5.3fS",
                    index, criterion.name(),
                    basis, criterion().weight(), weightedValue(),
                    deltaV(), deltaT());
            case direct,remix ->
                String.format(
                    "% 5d %30s  basis=% 12.3f ⋅ W=%1.1f C=% 17.5f",
                    index, criterion.name(),
                    basis, criterion().weight(), weightedValue());
        };
    }

}
