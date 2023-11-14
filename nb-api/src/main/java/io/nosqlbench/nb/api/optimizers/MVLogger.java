/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.nb.api.optimizers;

import org.apache.commons.math4.legacy.analysis.MultivariateFunction;
import java.util.ArrayList;
import java.util.List;

public class MVLogger implements MultivariateFunction {
    private final MultivariateFunction function;
    List<Entry> entries = new ArrayList<>();

    public MVLogger(MultivariateFunction function) {
        this.function = function;
    }

    @Override
    public double value(double[] params) {
        double value = function.value(params);
        entries.add(new Entry(params,value));
        return value;
    }

    public List<Entry> getLogList() {
        return entries;
    }

    public List<Entry> getLog() {
        return entries;
    }
    public double[][] getLogArray() {
        double[][] ary = new double[entries.size()][];
        for (int row = 0; row < entries.size(); row++) {
            Entry entry = entries.get(row);
            ary[row]=entry.params();
        }
        return ary;
    }

    public Entry getLastEntry() {
        return entries.getLast();
    }

    public static record Entry(double[] params, double value) {};
}
