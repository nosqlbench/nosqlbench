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

package io.nosqlbench.scenarios.findmax;

/**
 * Capture the control inputs as well as the samples of a sample period of a simulated workload.
 * @param params The parameters which control the simulated workload during the sample window
 * @param samples The measured samples, including key metrics and criteria for the sample window
 */
public record SimFrame(SimFrameParams params, FrameSampleSet samples) {
    public double value() {
        return samples().value();
    }
    public int index() {
        return samples.index();
    }


}
