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

package io.nosqlbench.scenarios.simframe.optimo;


import org.apache.commons.math4.legacy.optim.SimpleBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

public class OptimoParamModel {
    private final List<Param> params = new ArrayList<>();

    public OptimoParamModel add(String name, double min, double initial, double max, DoubleConsumer effector) {
        if (min>initial || initial > max) {
            throw new RuntimeException("parameters must be in min<initial<max order");
        }
        this.params.add(new Param(name, min, initial, max, effector));
        return this;
    }

    public OptimoFrameParams apply(double[] values) {
        for (int i = 0; i < values.length; i++) {
            params.get(i).effector.accept(values[i]);
        }
        return new OptimoFrameParams(this, values);
    }

    public SimpleBounds getBounds() {
        return new SimpleBounds(lowerBounds(),upperBounds());
    }

    public double[] getInitialGuess() {
        double[] initialGuess = new double[params.size()];
        for (int i = 0; i < params.size(); i++) {
            initialGuess[i]=params.get(i).initialGuess;
        }
        return initialGuess;
    }

    private double[] lowerBounds() {
        double[] lowerBounds = new double[params.size()];
        for (int i = 0; i < params.size(); i++) {
            lowerBounds[i]=params.get(i).lowerBound;
        }
        return lowerBounds;
    }

    private double[] upperBounds() {
        double[] upperBounds = new double[params.size()];
        for (int i = 0; i < params.size(); i++) {
            upperBounds[i]=params.get(i).upperBound;
        }
        return upperBounds;
    }

    public String summarizeParams(double[] paramValues) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            Param p = params.get(i);
            sb.append(String.format("%30s % 15.2f [%f-%f]\n", p.name, paramValues[i],p.lowerBound,p.upperBound));
        }
        return sb.toString();
    }

    public static record Param(
        String name,
        double lowerBound,
        double initialGuess,
        double upperBound,
        DoubleConsumer effector) {}
}
