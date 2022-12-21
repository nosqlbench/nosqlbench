/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.activity;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.RunState;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ActivityRuntimeInfo implements ProgressCapable {

    private final static Logger logger = LogManager.getLogger(ActivityRuntimeInfo.class);

    private final Activity activity;
    private final Future<ExecutionResult> future;
    private final ActivityExecutor executor;

    private ExecutionResult result;

    public ActivityRuntimeInfo(Activity activity, Future<ExecutionResult> result, ActivityExecutor executor) {

        this.activity = activity;
        this.future = result;
        this.executor = executor;
    }

    @Override
    public ProgressMeterDisplay getProgressMeter() {
        return this.activity.getProgressMeter();
    }


    public Future<ExecutionResult> getFuture() {
        return this.future;
    }

    /**
     * Wait until the execution is complete and return the result.
     * @param timeoutMillis
     * @return null, or an ExecutionResult if the execution completed
     */
    public ExecutionResult awaitResult(long timeoutMillis) {
        ExecutionResult result = null;
        try {
            result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
        } catch (InterruptedException ie) {
            logger.warn("interrupted waiting for execution to complete");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
//            return new ExecutionResult(activity.getStartedAtMillis(),System.currentTimeMillis(),"",e);
        }
        return result;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public boolean isRunning() {
        return executor.isRunning();
    }

    public RunState getRunState() {
        return this.activity.getRunState();
    }

    public void stopActivity() {
        this.executor.stopActivity();
    }

    public ActivityExecutor getActivityExecutor() {
        return executor;
    }
}
