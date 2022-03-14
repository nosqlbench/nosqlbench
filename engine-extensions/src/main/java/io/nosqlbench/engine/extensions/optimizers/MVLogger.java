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

package io.nosqlbench.engine.extensions.optimizers;

import org.apache.commons.math3.analysis.MultivariateFunction;

import java.util.ArrayList;
import java.util.List;

public class MVLogger implements MultivariateFunction {
    private final MultivariateFunction function;
    List<List<Double>> log = new ArrayList<>();

    public MVLogger(MultivariateFunction function) {
        this.function = function;
    }

    @Override
    public double value(double[] doubles) {
        ArrayList<Double> params = new ArrayList<>(doubles.length);
        log.add(params);

        return function.value(doubles);
    }

    public List<List<Double>> getLogList() {
        return log;
    }

    public double[][] getLogArray() {
        double[][] ary = new double[log.size()][];
        for (int row = 0; row < log.size(); row++) {
            List<Double> columns = log.get(row);
            double[] rowary = new double[columns.size()];
            ary[row]=rowary;
            for (int col = 0; col < log.get(row).size(); col++) {
                rowary[col]=columns.get(col);
            }
        }
        return ary;
    }
}
