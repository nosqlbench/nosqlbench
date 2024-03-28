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

package io.nosqlbench.scenarios.simframe.planning;

import io.nosqlbench.engine.core.lifecycle.scenario.container.InvokableResult;

public abstract class SimFrameFunctionAnalyzer<A extends SimFrameFunction<? extends InvokableResult>, C extends Record> {
    protected final A function;
    protected C config;

    protected SimFrameFunctionAnalyzer(A function, C config) {
        this.function = function;
        this.config = config;
    }

    public record FrameResult(double value, SimFrameAction action) {}

    public SimFrame<? extends InvokableResult> analyze() {
        FrameResult result = initialFrame();
        while (result.action() == SimFrameAction.continue_run) {
            result = nextFrame();
        }
        return function.getJournal().bestRun();
    }

    protected abstract FrameResult nextFrame();
    protected abstract FrameResult initialFrame();
}
