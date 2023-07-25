/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.core.progress;

public interface InputProgressMeter {
    String getProgressName();

    //    RunState getProgressState();
    long getStartedAtMillis();

    long getMinInputCycle();

    long getCurrentInputCycle();

    long getMaxInputCycle();

    long getRecyclesCurrent();

    long getRecyclesMax();

    default String getProgressSummary() {
        return "min=" + getMinInputCycle() + " cycle=" + getCurrentInputCycle() + " max=" + getMaxInputCycle() +
            (getRecyclesMax() > 0L ? " recycles=" + getRecyclesCurrent() + "/" + getRecyclesMax() : "");
    }

    default double getProgressRatio() {
        return
            ((double) (getCurrentInputCycle() - getMinInputCycle()))
                /
                ((double) (getMaxInputCycle() - getMinInputCycle()));
    }

    default double getProgressTotal() {
        return (getMaxInputCycle() - getMinInputCycle());
    }

    default double getProgressETAMillis() {
        long then = getStartedAtMillis();
        long now = System.currentTimeMillis();
        double elapsed = now - then;

        double completed = getCurrentInputCycle() - getMinInputCycle();
        double rate = completed / elapsed;

        double remaining = getMaxInputCycle() - getCurrentInputCycle();
        return remaining / rate;
    }
}

