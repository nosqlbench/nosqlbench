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

package io.nosqlbench.scenarios.simframe.optimizers.findmax;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;

public class FindmaxConfig {
    private double sample_time_ms;
    private double max_value;
    private double base_value;
    private double min_value;
    private double step_value;
    private double value_incr;
    private double sample_incr;
    private long min_settling_ms;
    private String optimization_type;

    public double sample_time_ms() {
        return sample_time_ms;
    }

    public double max_value() {
        return max_value;
    }

    public double base_value() {
        return base_value;
    }

    public double min_value() {
        return min_value;
    }

    public double step_value() {
        return step_value;
    }

    public double value_incr() {
        return value_incr;
    }

    public double sample_incr() {
        return sample_incr;
    }

    public long min_settling_ms() {
        return min_settling_ms;
    }

    public String optimization_type() {
        return optimization_type;
    }

    public void setSample_time_ms(double sample_time_ms) {
        this.sample_time_ms = sample_time_ms;
    }

    public void setMax_value(double max_value) {
        this.max_value = max_value;
    }

    public void setBase_value(double base_value) {
        this.base_value = base_value;
    }

    public void setStep_value(double step_value) {
        this.step_value = step_value;
    }

    public void setMin_value(double min_value) {
        this.min_value = min_value;
    }

    public void setSample_incr(double sample_incr) {
        this.sample_incr = sample_incr;
    }

    public void setValue_incr(double value_incr) {
        this.value_incr = value_incr;
    }

    public void setMin_settling_ms(long min_settling_ms) {
        this.min_settling_ms = min_settling_ms;
    }

    public void setOptimization_type(String optimization_type) {
        this.optimization_type = optimization_type;
    }

    public FindmaxConfig(NBCommandParams params) {
        setSample_time_ms(params.maybeGet("sample_time_ms").map(Double::parseDouble).orElse(4000d));
        setMax_value(params.maybeGet("max_value").map(Double::parseDouble).orElse(10000d));
        setBase_value(params.maybeGet("base_value").map(Double::parseDouble).orElse(10d));
        setMin_value(params.maybeGet("min_value").map(Double::parseDouble).orElse(0d));
        setStep_value(params.maybeGet("step_value").map(Double::parseDouble).orElse(100d));
        setValue_incr(params.maybeGet("value_incr").map(Double::parseDouble).orElse(2d));
        setSample_incr(params.maybeGet("sample_incr").map(Double::parseDouble).orElse(1.2d));
        setMin_settling_ms(params.maybeGet("min_settling_ms").map(Long::parseLong).orElse(4000L));
        setOptimization_type(params.maybeGet("optimization_type").orElse("rate"));
    }
}
