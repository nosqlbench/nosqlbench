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

package io.nosqlbench.engine.core;

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityimpl.CoreServices;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.action.CoreActionDispenser;
import io.nosqlbench.engine.api.activityimpl.input.CoreInputDispenser;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotor;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotorDispenser;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityExecutor;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityTypeLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ActivityExecutorTest {
    private static final Logger logger = LogManager.getLogger(ActivityExecutorTest.class);

// TODO: Design review of this mechanism
//    @Test
//    synchronized void testRestart() {
//        ActivityDef activityDef = ActivityDef.parseActivityDef("driver=diag;alias=test-restart;cycles=1000;cyclerate=10;op=initdelay:initdelay=5000;");
//        new ActivityTypeLoader().load(activityDef);
//
//        final Activity activity = new DelayedInitActivity(activityDef);
//        InputDispenser inputDispenser = new CoreInputDispenser(activity);
//        ActionDispenser adisp = new CoreActionDispenser(activity);
//        OutputDispenser tdisp = CoreServices.getOutputDispenser(activity).orElse(null);
//
//        final MotorDispenser<?> mdisp = new CoreMotorDispenser(activity, inputDispenser, adisp, tdisp);
//        activity.setActionDispenserDelegate(adisp);
//        activity.setOutputDispenserDelegate(tdisp);
//        activity.setInputDispenserDelegate(inputDispenser);
//        activity.setMotorDispenserDelegate(mdisp);
//
//        final ExecutorService executor = Executors.newCachedThreadPool();
//        ActivityExecutor activityExecutor = new ActivityExecutor(activity, "test-restart");
//        final Future<ExecutionResult> future = executor.submit(activityExecutor);
//        try {
//            activityDef.setThreads(1);
//            activityExecutor.startActivity();
//            Thread.sleep(100L);
//            activityExecutor.stopActivity();
//            Thread.sleep(100L);
//            activityExecutor.startActivity();
//            Thread.sleep(100L);
//            activityExecutor.stopActivity();
//            future.get();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        executor.shutdown();
//        assertThat(inputDispenser.getInput(10).getInputSegment(3)).isNotNull();
//
//    }

    @Test
    synchronized void testDelayedStartSanity() {

        ActivityDef activityDef = ActivityDef.parseActivityDef("driver=diag;alias=test-delayed-start;cycles=1000;initdelay=2000;");
        new ActivityTypeLoader().load(activityDef, TestComponent.INSTANCE);

        Activity activity = new DelayedInitActivity(activityDef);
        final InputDispenser inputDispenser = new CoreInputDispenser(activity);
        final ActionDispenser actionDispenser = new CoreActionDispenser(activity);
        final OutputDispenser outputDispenser = CoreServices.getOutputDispenser(activity).orElse(null);

        MotorDispenser<?> motorDispenser = new CoreMotorDispenser(activity, inputDispenser, actionDispenser, outputDispenser);
        activity.setActionDispenserDelegate(actionDispenser);
        activity.setOutputDispenserDelegate(outputDispenser);
        activity.setInputDispenserDelegate(inputDispenser);
        activity.setMotorDispenserDelegate(motorDispenser);

        ActivityExecutor activityExecutor = new ActivityExecutor(activity, "test-delayed-start");

        ExecutorService testExecutor = Executors.newCachedThreadPool();
        Future<ExecutionResult> future = testExecutor.submit(activityExecutor);

        try {
            activityDef.setThreads(1);
            future.get();
            testExecutor.shutdownNow();

        } catch (final Exception e) {
            fail("Unexpected exception", e);
        }

        assertThat(inputDispenser.getInput(10).getInputSegment(3)).isNull();
    }

    @Test
    synchronized void testNewActivityExecutor() {

        final ActivityDef activityDef = ActivityDef.parseActivityDef("driver=diag;alias=test-dynamic-params;cycles=1000;initdelay=5000;");
        new ActivityTypeLoader().load(activityDef,TestComponent.INSTANCE);

        Activity simpleActivity = new SimpleActivity(TestComponent.INSTANCE,activityDef);

//        this.getActivityMotorFactory(this.motorActionDelay(999), new AtomicInput(simpleActivity,activityDef));

        final InputDispenser inputDispenser = new CoreInputDispenser(simpleActivity);
        final ActionDispenser actionDispenser = new CoreActionDispenser(simpleActivity);
        final OutputDispenser outputDispenser = CoreServices.getOutputDispenser(simpleActivity).orElse(null);

        MotorDispenser<?> motorDispenser = new CoreMotorDispenser<>(simpleActivity,
                inputDispenser, actionDispenser, outputDispenser);

        simpleActivity.setActionDispenserDelegate(actionDispenser);
        simpleActivity.setInputDispenserDelegate(inputDispenser);
        simpleActivity.setMotorDispenserDelegate(motorDispenser);

        ActivityExecutor activityExecutor = new ActivityExecutor(simpleActivity, "test-new-executor");
        activityDef.setThreads(5);
        ForkJoinTask<ExecutionResult> executionResultForkJoinTask = ForkJoinPool.commonPool().submit(activityExecutor);

//        activityExecutor.startActivity();

        final int[] speeds = {1, 50, 5, 50, 2, 50};
        for (int offset = 0; offset < speeds.length; offset += 2) {
            final int threadTarget = speeds[offset];
            final int threadTime = speeds[offset + 1];

            ActivityExecutorTest.logger.debug(() -> "Setting thread level to " + threadTarget + " for " + threadTime + " seconds.");
            activityDef.setThreads(threadTarget);

            try {
                Thread.sleep(threadTime);
            } catch (final Exception e) {
                fail("Not expecting exception", e);
            }
        }
        executionResultForkJoinTask.cancel(true);

        // Used for slowing the roll due to state transitions in test.
        try {
            activityExecutor.stopActivity();
//            Thread.sleep(2000L);
        } catch (final Exception e) {
            fail("Not expecting exception", e);
        }
    }

    private MotorDispenser<?> getActivityMotorFactory(final Action lc, Input ls) {
        return new MotorDispenser<>() {
            @Override
            public Motor getMotor(final ActivityDef activityDef, final int slotId) {
                final Activity activity = new SimpleActivity(TestComponent.INSTANCE,activityDef);
                final Motor<?> cm = new CoreMotor<>(activity, slotId, ls);
                cm.setAction(lc);
                return cm;
            }
        };
    }

    private SyncAction motorActionDelay(long delay) {
        return new SyncAction() {
            @Override
            public int runCycle(final long cycle) {
                ActivityExecutorTest.logger.info(() -> "consuming " + cycle + ", delaying:" + delay);
                try {
                    Thread.sleep(delay);
                } catch (final InterruptedException ignored) {
                }
                return 0;
            }
        };

    }

    private static class DelayedInitActivity extends SimpleActivity {
        private static final Logger logger = LogManager.getLogger(DelayedInitActivity.class);

        public DelayedInitActivity(final ActivityDef activityDef) {
            super(TestComponent.INSTANCE,activityDef);
        }

        @Override
        public void initActivity() {
            final Integer initDelay = this.activityDef.getParams().getOptionalInteger("initdelay").orElse(0);
            DelayedInitActivity.logger.info(() -> "delaying for " + initDelay);
            try {
                Thread.sleep(initDelay);
            } catch (final InterruptedException ignored) {
            }
            DelayedInitActivity.logger.info(() -> "delayed for " + initDelay);
        }
    }
}
