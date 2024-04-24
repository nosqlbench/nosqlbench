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

package io.nosqlbench.virtdata.userlibs.apps.summarizer;

import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class DataSetSummary<T> {
    private final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

    private String source;
    private final ToDoubleFunction<T> toDoubleF;

    public DataSetSummary(ToDoubleFunction<T> toDoubleF) {
        this.toDoubleF = toDoubleF;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getSource() {
        return this.source;
    }

    public void addObject(Object o) {
        double value = toDoubleF.applyAsDouble((T) o);
        stats.accept(value);
    }
    private void add(double value) {
        stats.accept(value);
    }

    public String toString() {
        return source + ": " + Map.of(
            "count", stats.getCount(),
            "min", stats.getMin(),
            "max", stats.getMax(),
            "average", stats.getAverage(),
            "sum", stats.getSum()
        );
    }

    public static DoubleSummaryStatistics reduce(DataSetSummary<?> left, DataSetSummary<?> right) {
        var thisdata=left.getSummaryStats();
        var thatdata=right.getSummaryStats();
        DoubleSummaryStatistics newstats = new DoubleSummaryStatistics();
        newstats.combine(thisdata);
        newstats.combine(thatdata);
        return newstats;
    }

    public DoubleSummaryStatistics getSummaryStats() {
        return this.stats;
    }
}
