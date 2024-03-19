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
    private double rate_base;
    private double rate_step;
    private double rate_incr;
    private double sample_incr;
    private long min_settling_ms;

    public double sample_time_ms() {
        return sample_time_ms;
    }

    public double sample_max() {
        return sample_max;
    }

    public double rate_base() {
        return rate_base;
    }

    public double rate_step() {
        return rate_step;
    }

    public double rate_incr() {
        return rate_incr;
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

    public void setRate_base(double rate_base) {
        this.rate_base = rate_base;
    }

    public void setRate_step(double rate_step) {
        this.rate_step = rate_step;
    }

    public void setSample_incr(double sample_incr) {
        this.sample_incr = sample_incr;
    }

    public void setRate_incr(double rate_incr) {
        this.rate_incr = rate_incr;
    }

    public void setMin_settling_ms(long min_settling_ms) {
        this.min_settling_ms = min_settling_ms;
    }

    public FindmaxConfig(NBCommandParams params) {
        params.maybeGet("sample_time_ms").map(Double::parseDouble).orElse(4000d);
        params.maybeGet("sample_max").map(Double::parseDouble).orElse(10000d);
        params.maybeGet("rate_base").map(Double::parseDouble).orElse(10d);
        params.maybeGet("rate_step").map(Double::parseDouble).orElse(100d);
        params.maybeGet("rate_incr").map(Double::parseDouble).orElse(2d);
        params.maybeGet("sample_incr").map(Double::parseDouble).orElse(1.2d);
        params.maybeGet("min_settling_ms").map(Long::parseLong).orElse(4000L);
    }
}
