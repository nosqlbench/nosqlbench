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
    private double sample_max;
    private double base_value;
    private double step_value;
    private double value_incr;
    private double sample_incr;
    private long min_settling_ms;

    public double sample_time_ms() {
        return sample_time_ms;
    }

    public double sample_max() {
        return sample_max;
    }

    public double base_value() {
        return base_value;
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

    public void setSample_time_ms(double sample_time_ms) {
        this.sample_time_ms = sample_time_ms;
    }

    public void setSample_max(double sample_max) {
        this.sample_max = sample_max;
    }

    public void setBase_value(double base_value) {
        this.base_value = base_value;
    }

    public void setStep_value(double step_value) {
        this.step_value = step_value;
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

    public FindmaxConfig(NBCommandParams params) {
        setSample_time_ms(params.maybeGet("sample_time_ms").map(Double::parseDouble).orElse(4000d));
        setSample_max(params.maybeGet("sample_max").map(Double::parseDouble).orElse(10000d));
        setBase_value(params.maybeGet("base_value").map(Double::parseDouble).orElse(10d));
        setStep_value(params.maybeGet("step_value").map(Double::parseDouble).orElse(100d));
        setValue_incr(params.maybeGet("value_incr").map(Double::parseDouble).orElse(2d));
        setSample_incr(params.maybeGet("sample_incr").map(Double::parseDouble).orElse(1.2d));
        setMin_settling_ms(params.maybeGet("min_settling_ms").map(Long::parseLong).orElse(4000L));
    }
}
