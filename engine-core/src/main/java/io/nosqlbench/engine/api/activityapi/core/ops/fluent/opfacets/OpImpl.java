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

package io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets;

public class OpImpl<D> implements OpFacets<D> {

//    private OpTracker<D> tracker;

    private D data;
    private long cycle;
    private int cycleResult;

    private long waitTime;
    private long endedAtNanos;
    private long startedAtNanos;

    //    private long usages;
    private int tries = 0;
    private int skipreason;


    public OpImpl() {
    }

    @Override
    public StartedOp<D> start() {
        this.endedAtNanos = Long.MIN_VALUE;
        this.startedAtNanos = System.nanoTime();
        tries = 1;
        return this;
    }


    @Override
    public OpImpl<D> setWaitTime(long waitTime) {
        this.endedAtNanos = Long.MIN_VALUE;
        this.waitTime = waitTime;
        this.startedAtNanos = System.nanoTime();
//        usages++;
        return this;
    }

    @Override
    public SucceededOp<D> succeed(int status) {
        // TODO: Enable a debug version of OpImpl which can assert invariants (succeed is only called once after start, ...)
        this.endedAtNanos = System.nanoTime();
        this.cycleResult = status;
        return this;
    }

    @Override
    public FailedOp<D> fail(int status) {
        this.endedAtNanos = System.nanoTime();
        this.cycleResult = status;
        return this;
    }

    @Override
    public StartedOp<D> retry() {
        this.startedAtNanos = System.nanoTime();
        this.endedAtNanos = Long.MIN_VALUE;
        tries++;
        return this;
    }

    @Override
    public SkippedOp<D> skip(int reason) {
        this.skipreason=reason;
        return this;
    }

    @Override
    public long getCycle() {
        return this.cycle;
    }

    @Override
    public void setCycle(long cycle) {
        this.cycle = cycle;
    }

    @Override
    public long getStartedAtNanos() {
        return startedAtNanos;
    }

    @Override
    public D getOpData() {
        return data;
    }

    @Override
    public void setData(D data) {
        this.data = data;
    }

    @Override
    public int getTries() {
        return tries;
    }

    @Override
    public long getCurrentServiceTimeNanos() {
        return System.nanoTime() - this.startedAtNanos;
    }

    @Override
    public long getCurrentResponseTimeNanos() {
        return waitTime + getCurrentServiceTimeNanos();
    }

    @Override
    public long getServiceTimeNanos() {
        return this.endedAtNanos - this.startedAtNanos;
    }

    @Override
    public long getResponseTimeNanos() {
        return waitTime + getServiceTimeNanos();
    }

    @Override
    public int getResult() {
        return this.cycleResult;
    }

    @Override
    public String toString() {
        return "Op{" +
                "cycle=" + cycle +
                ", result=" + cycleResult +
                ", wait=" + waitTime +
                ", started=" + startedAtNanos +
                ", ended=" + endedAtNanos +
                ", tries=" + tries +
                ", data=" + (data == null ? "NULL" : data.toString()) +
                '}';
    }

    @Override
    public int getSkippedReason() {
        return skipreason;
    }
}
