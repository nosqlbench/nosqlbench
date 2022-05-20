package io.nosqlbench.engine.extensions.optimizers;

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


import java.util.HashMap;
import java.util.Map;

public class MVResult {

    private final double[] vars;
    private final MVParams params;
    private final double[][] datalog;

    public MVResult(double[] vars, MVParams params, double[][] datalog) {
        this.params = params;
        this.vars = vars;
        this.datalog = datalog;
    }

    public double[] getVarArray() {
        return vars;
    }

    public double[][] getDatalog() {
        return datalog;
    }

    public Map<String, Double> getVars() {
        Map<String, Double> result = new HashMap<>(params.size());
        for (int i = 0; i < vars.length; i++) {
            result.put(params.get(i).name, vars[i]);
        }
        return result;
    }

    public Map<String,Map<String,Double>> getMap() {
        Map<String,Map<String,Double>> map = new HashMap<>();
        for (int i = 0; i < params.size(); i++) {
            Map<String,Double> entry = Map.of(
                "min", params.get(i).min,
                "max", params.get(i).max,
                "value", this.vars[i]
            );
            map.put(params.get(i).name, entry);
        }
        return map;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (MVParams.MVParam param : params) {
            sb.append(param.name).append("=").append(vars[pos])
                    .append(" [").append(param.min).append("..").append(param.max)
                    .append("]");
            sb.append("\n");
            pos++;
        }
        sb.setLength(sb.length()-1);

        return sb.toString();
    }
}
