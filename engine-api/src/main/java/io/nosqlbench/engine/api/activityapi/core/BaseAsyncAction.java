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

package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @param <D> An type of state holder for an operation, holding everything unique to that cycle and operation
 * @param <A> An type of of an Activity, a state holder for a runtime instance of an Activity
 */
public abstract class BaseAsyncAction<D, A extends Activity> implements AsyncAction<D>, Stoppable, ActivityDefObserver {
    private final static Logger logger = LogManager.getLogger("BaseAsyncAction");

    protected final A activity;

    protected int slot;
    protected boolean running = true;

    public BaseAsyncAction(A activity, int slot) {
        this.activity = activity;
        this.slot = slot;

        onActivityDefUpdate(activity.getActivityDef());
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        ParameterMap params = activityDef.getParams();
        params.getOptionalInteger("async").orElseThrow(
                () -> new RuntimeException("the async parameter is required to activate async actions"));
    }

    public boolean enqueue(TrackedOp<D> opc) {
        startOpCycle(opc);
        return (running);
    }

    /**
     * Implementations that extend this base class can call this method in order to put
     * an operation in flight. Implementations should call either {@link TrackedOp#skip(int)}
     * or {@link TrackedOp#start()}}.
     *
     * @param opc A tracked operation with state of parameterized type D
     */
    public abstract void startOpCycle(TrackedOp<D> opc);

    @Override
    public void requestStop() {
        logger.info(() -> this + " requested to stop.");
        this.running = false;
    }

}
