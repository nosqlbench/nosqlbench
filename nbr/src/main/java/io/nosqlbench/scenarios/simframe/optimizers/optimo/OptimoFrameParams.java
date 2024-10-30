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

package io.nosqlbench.scenarios.simframe.optimizers.optimo;


import io.nosqlbench.engine.core.lifecycle.scenario.container.InvokableResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.scenarios.simframe.planning.GenericParamModel;

import java.util.LinkedHashMap;
import java.util.Map;

public class OptimoFrameParams implements InvokableResult {

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

    @Override
    public Map<String, String> asResult() {
        Map<String,String> result = new LinkedHashMap<>();
        for (int i = 0; i < this.paramValues.length; i++) {
            result.put(model.getParams().get(i).name(),String.valueOf(paramValues[i]));
        }
        return result;
    }
}
