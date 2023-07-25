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

package io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results;

public class MutableCycleResult implements CycleResult {

    private final long cycle;
    private int result;
    private long startTimeNanos;
    private long endTimeNanos=Long.MIN_VALUE; // you will get some strange results if you forget to update this
    private final long schedulingDelay;

    public MutableCycleResult(long cycle, int result, long startTimeNanos, long schedulingDelay) {
        this.cycle = cycle;
        this.result = result;
        this.startTimeNanos = startTimeNanos;
        this.schedulingDelay=schedulingDelay;
    }
    public MutableCycleResult(long cycle, int result, long startTimeNanos) {
        this(cycle,result,startTimeNanos,Long.MIN_VALUE);
    }

    public MutableCycleResult(long cycle, int result) {
        this(cycle,result, System.nanoTime());
    }

    @Override
    public long getCycle() {
        return cycle;
    }

    @Override
    public int getResult() {
        return result;
    }

    public String toString() {
        return this.cycle +"->" + this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public void setStartTimeNanos(long startTimeNanos) {
        this.startTimeNanos = startTimeNanos;
    }

    public long getEndTimeNanos() {
        return endTimeNanos;
    }

    public void setEndTimeNanos(long endTimeNanos) {
        this.endTimeNanos = endTimeNanos;
    }

    public long getOpNanos() {
        return schedulingDelay + (endTimeNanos - startTimeNanos);
    }

}
