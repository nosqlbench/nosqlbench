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


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OptimoFrameParams{

    OptimoParamModel model;
    double[] paramValues;

    public OptimoFrameParams(OptimoParamModel model, double[] paramValues) {
        this.model = model;
        this.paramValues = paramValues;
    }

    @Override
    public String toString() {
        return model.summarizeParams(paramValues);
    }

    public double[] paramValues() {
        return paramValues;
    }

}
