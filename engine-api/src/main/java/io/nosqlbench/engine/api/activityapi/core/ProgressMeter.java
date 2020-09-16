/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityapi.core;

public interface ProgressMeter {
    String getProgressName();

    //    RunState getProgressState();
    long getStartedAtMillis();

    long getProgressMin();

    long getProgressCurrent();

    long getProgressMax();

    long getRecyclesCurrent();

    long getRecyclesMax();

    default String getProgressSummary() {
        return "min=" + getProgressMin() + " cycle=" + getProgressCurrent() + " max=" + getProgressMax() +
            (getRecyclesMax() > 0L ? " recycles=" + getRecyclesCurrent() + "/" + getRecyclesMax() : "");
    }

    default double getProgressRatio() {
        return
            ((double) (getProgressCurrent() - getProgressMin()))
                /
                ((double) (getProgressMax() - getProgressMin()));
    }

    default double getProgressTotal() {
        return (getProgressMax() - getProgressMin());
    }

    default double getProgressETAMillis() {
        long then = getStartedAtMillis();
        long now = System.currentTimeMillis();
        double elapsed = now - then;

        double completed = getProgressCurrent() - getProgressMin();
        double rate = completed / elapsed;

        double remaining = getProgressMax() - getProgressCurrent();
        return remaining / rate;
    }
}

