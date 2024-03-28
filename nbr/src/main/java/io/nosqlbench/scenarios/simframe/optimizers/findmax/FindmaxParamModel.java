/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.scenarios.simframe.optimizers.findmax;

import io.nosqlbench.scenarios.simframe.planning.GenericParamModel;
import org.apache.commons.math4.legacy.optim.SimpleBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

public class FindmaxParamModel {
    private final List<GenericParamModel> params = new ArrayList<>();

    public FindmaxParamModel add(String name, double min, double initial, double max, DoubleConsumer effector) {
        if (min>initial || initial > max) {
            throw new RuntimeException("parameters must be in min<initial<max order, but " + name + " was min=" + min +
                ", initial=" + initial + ", max=" + max);
        }
        this.params.add(new GenericParamModel(name, min, initial, max, effector));
        return this;
    }

    public FindmaxFrameParams apply(double[] values) {
        for (int i = 0; i < values.length; i++) {
            params.get(i).effector().accept(values[i]);
        }
        return new FindmaxFrameParams(this, values);
    }

    //TODO: Unless this changes in development everything from here on down can be abstracted from here and Optimo
    //      and put into a super class
    public SimpleBounds getBounds() {
        return new SimpleBounds(lowerBounds(),upperBounds());
    }

    public double[] getInitialGuess() {
        double[] initialGuess = new double[params.size()];
        for (int i = 0; i < params.size(); i++) {
            initialGuess[i]=params.get(i).initialGuess();
        }
        return initialGuess;
    }

    private double[] lowerBounds() {
        double[] lowerBounds = new double[params.size()];
        for (int i = 0; i < params.size(); i++) {
            lowerBounds[i]=params.get(i).lowerBound();
        }
        return lowerBounds;
    }

    private double[] upperBounds() {
        double[] upperBounds = new double[params.size()];
        for (int i = 0; i < params.size(); i++) {
            upperBounds[i]=params.get(i).upperBound();
        }
        return upperBounds;
    }

    public String summarizeParams(double[] paramValues) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            GenericParamModel p = params.get(i);
            sb.append(String.format("%30s % 15.2f [%f-%f]\n", p.name(), paramValues[i],p.lowerBound(),p.upperBound()));
        }
        return sb.toString();
    }

    public List<GenericParamModel> getParams() {
        return this.params;
    }
}
