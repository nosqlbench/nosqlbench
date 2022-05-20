package io.nosqlbench.virtdata.library.basics.shared.distributions;

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


class LabeledStatistic {
    public final String label;
    public final double total;
    public final int count;
    public final double min;
    public final double max;

    public LabeledStatistic(String label, double weight) {
        this.label = label;
        this.total = weight;
        this.min = weight;
        this.max = weight;
        this.count = 1;
    }

    private LabeledStatistic(String label, double total, double min, double max, int count) {
        this.label = label;
        this.total = total;
        this.min = min;
        this.max = max;
        this.count = count;
    }

    public LabeledStatistic merge(LabeledStatistic tuple) {
        return new LabeledStatistic(
            this.label,
            this.total + tuple.total,
            Math.min(this.min, tuple.min),
            Math.max(this.max, tuple.max),
            this.count + tuple.count
        );
    }

    public double count() {
        return count;
    }

    public double avg() {
        return total / count;
    }

    public double sum() {
        return total;
    }

    @Override
    public String toString() {
        return "EntryTuple{" +
            "label='" + label + '\'' +
            ", total=" + total +
            ", count=" + count +
            '}';
    }

    public double min() {
        return this.min;
    }

    public double max() {
        return this.max;
    }
}
