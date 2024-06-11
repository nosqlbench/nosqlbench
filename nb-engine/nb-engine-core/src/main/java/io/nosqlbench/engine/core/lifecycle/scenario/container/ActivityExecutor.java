/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.scenario.container;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.Startable;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResultSegmentBuffer;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.activity.LifetimeThread;

import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;

public class ActivityExecutor implements Callable<ExecutionResult> {
    private final Activity activity;
    private LifetimeThread lifetimeThread;

    StructuredTaskScope<ExecutionResult> scope;
    public ActivityExecutor(Activity activity) {
        this.activity = activity;
    }

    @Override
    public ExecutionResult call() throws Exception {
        long startedAt=System.currentTimeMillis();
        long endedAt=0L;
        Exception error = null;
        try (StructuredTaskScope.ShutdownOnFailure t= new StructuredTaskScope.ShutdownOnFailure()) {
            lifetimeThread = new LifetimeThread(activity.getAlias());
            t.fork(lifetimeThread);
            runCycles(t,activity);
            lifetimeThread = new LifetimeThread(activity.getAlias());
            t.join();
        } catch (Exception e) {
            error=e;

        } finally {
            endedAt=System.currentTimeMillis();
        }
        return new ExecutionResult(startedAt,endedAt,"",error);
    }

    private void runCycles(StructuredTaskScope.ShutdownOnFailure t, Activity activity) {
        Input input = activity.getInputDispenserDelegate().getInput();
        if (input instanceof Startable) {
            ((Startable) input).start();
        }

        CycleSegment cycleSegment = null;
        int stride = activity.getActivityDef().getParams().getOptionalInteger("stride").orElse(1);
        CycleResultSegmentBuffer segBuffer = new CycleResultSegmentBuffer(stride);

    }
}
