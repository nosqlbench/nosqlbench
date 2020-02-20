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

package io.nosqlbench.activityapi.core.ops.fluent.opfacets;

public class EventedOpImpl<D> extends OpImpl<D> {

    private OpEvents<D> opTracker;
    private OpEvents<D> strideTracker;

    public EventedOpImpl(OpEvents<D> opTracker, OpEvents<D> strideTracker) {
        this.opTracker = opTracker;
        this.strideTracker = strideTracker;
    }

    public EventedOpImpl(OpEvents<D> opTracker) {
        this.opTracker = opTracker;
        this.strideTracker = new NullTracker<>();
    }

    @Override
    public StartedOp<D> start() {
        super.start();
        opTracker.onOpStarted(this);
        strideTracker.onOpStarted(this);
        return this;
    }

    @Override
    public SucceededOp<D> succeed(int status) {
        super.succeed(status);
        opTracker.onOpSuccess(this);
        strideTracker.onOpSuccess(this);
        return this;
    }

    @Override
    public FailedOp<D> fail(int status) {
        super.fail(status);
        opTracker.onOpFailure(this);
        strideTracker.onOpFailure(this);
        return this;
    }

    private static class NullTracker<D> implements OpEvents<D> {
        @Override
        public void onOpStarted(StartedOp<D> op) {
        }

        @Override
        public void onOpSuccess(SucceededOp<D> op) {
        }

        @Override
        public void onOpSkipped(SkippedOp<D> op) {
        }

        @Override
        public void onOpFailure(FailedOp<D> op) {
        }
    }


}
