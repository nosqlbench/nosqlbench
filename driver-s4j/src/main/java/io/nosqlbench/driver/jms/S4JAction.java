package io.nosqlbench.driver.jms;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.codahale.metrics.Timer;
import io.nosqlbench.driver.jms.ops.S4JOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class S4JAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(S4JAction.class);

    private final S4JActivity activity;
    private final int slot;

    int maxTries;

    public S4JAction(S4JActivity activity, int slot) {
        this.activity = activity;
        this.slot = slot;
        this.maxTries = activity.getActivityDef().getParams().getOptionalInteger("maxtries").orElse(10);
    }

    @Override
    public void init() { }

    @Override
    public int runCycle(long cycle) {
        // let's fail the action if some async operation failed
        activity.failOnAsyncOperationFailure();

        long start = System.nanoTime();

        S4JOp s4JOp;
        try (Timer.Context ctx = activity.getBindTimer().time()) {
            LongFunction<S4JOp> readyJmsOp = activity.getSequencer().apply(cycle);
            s4JOp = readyJmsOp.apply(cycle);
        } catch (Exception bindException) {
            // if diagnostic mode ...
            activity.getErrorhandler().handleError(bindException, cycle, 0);
            throw new RuntimeException(
                "while binding request in cycle " + cycle + ": " + bindException.getMessage(), bindException
            );
        }

        for (int i = 0; i < maxTries; i++) {
            Timer.Context ctx = activity.getExecuteTimer().time();
            try {
                // it is up to the s4JOp to call Context#close when the activity is executed
                // this allows us to track time for async operations
                s4JOp.run(ctx::close);
                break;
            } catch (RuntimeException err) {
                ErrorDetail errorDetail = activity
                    .getErrorhandler()
                    .handleError(err, cycle, System.nanoTime() - start);
                if (!errorDetail.isRetryable()) {
                    break;
                }
            }
        }

        return 0;
    }
}
