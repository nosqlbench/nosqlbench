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
 *
 */

package io.nosqlbench.scenarios.simframe.optimizers.findmax;

import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFrameAction;
import io.nosqlbench.scenarios.simframe.planning.SimFrameFunctionAnalyzer;

import java.util.Comparator;

import static io.nosqlbench.virtdata.core.bindings.VirtDataLibrary.logger;

public class FindmaxAnalyzer extends SimFrameFunctionAnalyzer<FindmaxFrameFunction,FindmaxConfig> {

    public FindmaxAnalyzer(FindmaxFrameFunction function, FindmaxConfig config) {
        super(function, config);
    }

    @Override
    protected FrameResult nextFrame() {
        double newValue;
        SimFrame<FindmaxFrameParams> last = function.getJournal().last();
        SimFrame<FindmaxFrameParams> best = function.getJournal().bestRun();
        if (best.index() == last.index()) { // got better consecutively
            newValue = last.params().paramValues()[0] + config.step_value();
            config = new FindmaxConfig(
                config.sample_time_ms(),
                config.max_value(),
                config.base_value(),
                config.min_value(),
                (config.step_value() * config.value_incr()),
                config.value_incr(),
                config.sample_incr(),
                config.min_settling_ms(),
                config.optimization_type(),
                new double[]{newValue}
            );
        } else if (best.index() == last.index() - 1) {
            // got worse consecutively, this may be collapsed out since the general case below covers it (test first)
            if (((last.params().paramValues()[0] + config.step_value()) -
                (best.params().paramValues()[0] + config.step_value())) <= config.step_value()) {
                logger.info("could not divide search space further, stop condition met");
                return new FrameResult(best.params().paramValues()[0], SimFrameAction.stop_run);
            } else {
                newValue = best.params().paramValues()[0] + config.step_value();
                config = new FindmaxConfig(
                    (config.sample_time_ms() * config.sample_incr()),
                    config.max_value(),
                    config.base_value(),
                    config.min_value(),
                    config.step_value(),
                    config.value_incr(),
                    config.sample_incr(),
                    (config.min_settling_ms() * 4),
                    config.optimization_type(),
                    new double[]{newValue}
                );
            }
        } else { // any other case
            // find next frame with higher rate but lower value, the closest one by rate
            SimFrame<FindmaxFrameParams> nextWorseFrameWithHigherRate = function.getJournal().frames().stream()
                .filter(f -> f.value() < best.value())
                .filter(f -> f.params().paramValues()[0] + config.step_value() > (best.params().paramValues()[0] + config.step_value()))
                .min(Comparator.comparingDouble(f -> f.params().paramValues()[0] + config.step_value()))
                .orElseThrow(() -> new RuntimeException("inconsistent samples"));
            if ((nextWorseFrameWithHigherRate.params().paramValues()[0] + config.step_value() -
                best.params().paramValues()[0] + config.step_value()) > config.step_value()) {
                newValue = best.params().paramValues()[0] + config.step_value();
                config = new FindmaxConfig(
                    (config.sample_time_ms() * config.sample_incr()),
                    config.max_value(),
                    config.base_value(),
                    config.min_value(),
                    config.step_value(),
                    config.value_incr(),
                    config.sample_incr(),
                    (config.min_settling_ms() * 2),
                    config.optimization_type(),
                    new double[]{newValue}
                );
            } else {
                logger.info("could not divide search space further, stop condition met");
                return new FrameResult(best.params().paramValues()[0], SimFrameAction.stop_run);
            }
        }
        double[] point = {newValue};
        return new FrameResult(function.value(point), SimFrameAction.continue_run);
    }

    @Override
    protected FrameResult initialFrame() {
        return new FrameResult(function.value(config.initialPoint()), SimFrameAction.continue_run);
    }

}
