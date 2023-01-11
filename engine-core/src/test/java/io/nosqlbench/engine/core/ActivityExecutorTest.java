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

package io.nosqlbench.engine.core;

import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityimpl.CoreServices;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.action.CoreActionDispenser;
import io.nosqlbench.engine.api.activityimpl.input.AtomicInput;
import io.nosqlbench.engine.api.activityimpl.input.CoreInputDispenser;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotor;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotorDispenser;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityExecutor;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityTypeLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityExecutorTest {
    private static final Logger logger = LogManager.getLogger(ActivityExecutorTest.class);

    @Test
    public synchronized void testRestart() {
        ActivityDef ad = ActivityDef.parseActivityDef("driver=diag;alias=test;cycles=1000;op=initdelay:initdelay=5000;");
        Optional<ActivityType> activityType = new ActivityTypeLoader().load(ad);
        Activity a = new DelayedInitActivity(ad);
        InputDispenser idisp = new CoreInputDispenser(a);
        ActionDispenser adisp = new CoreActionDispenser(a);
        OutputDispenser tdisp = CoreServices.getOutputDispenser(a).orElse(null);
        MotorDispenser<?> mdisp = new CoreMotorDispenser(a, idisp, adisp, tdisp);
        a.setActionDispenserDelegate(adisp);
        a.setOutputDispenserDelegate(tdisp);
        a.setInputDispenserDelegate(idisp);
        a.setMotorDispenserDelegate(mdisp);
        ExecutorService executor = Executors.newCachedThreadPool();
        ActivityExecutor ae = new ActivityExecutor(a, "test-restart");
        Future<ExecutionResult> future = executor.submit(ae);
        try {
            ad.setThreads(1);
            ae.startActivity();
            ae.stopActivity(false);
            ae.startActivity();
            ae.startActivity();
            ExecutionResult executionResult = future.get();
            Thread.sleep(500L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.print("ad.setThreads(1)");
        executor.shutdown();
        assertThat(idisp.getInput(10).getInputSegment(3)).isNull();

    }

    @Test
    public synchronized void testDelayedStartSanity() {
        ActivityDef ad = ActivityDef.parseActivityDef("driver=diag;alias=test;cycles=1000;initdelay=5000;");
        Optional<ActivityType> activityType = new ActivityTypeLoader().load(ad);
        Activity a = new DelayedInitActivity(ad);
        InputDispenser idisp = new CoreInputDispenser(a);
        ActionDispenser adisp = new CoreActionDispenser(a);
        OutputDispenser tdisp = CoreServices.getOutputDispenser(a).orElse(null);
        MotorDispenser<?> mdisp = new CoreMotorDispenser(a, idisp, adisp, tdisp);
        a.setActionDispenserDelegate(adisp);
        a.setOutputDispenserDelegate(tdisp);
        a.setInputDispenserDelegate(idisp);
        a.setMotorDispenserDelegate(mdisp);

        ActivityExecutor ae = new ActivityExecutor(a, "test-delayed-start");
        ExecutorService testExecutor = Executors.newCachedThreadPool();
        Future<ExecutionResult> future = testExecutor.submit(ae);

        try {
            ad.setThreads(1);
            ae.startActivity();
            ExecutionResult result = future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        testExecutor.shutdownNow();
        assertThat(idisp.getInput(10).getInputSegment(3)).isNull();

    }

    @Test
    public synchronized void testNewActivityExecutor() {
        ActivityDef ad = ActivityDef.parseActivityDef("driver=diag;alias=test;cycles=1000;");
        Optional<ActivityType> activityType = new ActivityTypeLoader().load(ad);
        Input longSupplier = new AtomicInput(ad);
        MotorDispenser<?> cmf = getActivityMotorFactory(
            ad, motorActionDelay(999), longSupplier
        );
        Activity a = new SimpleActivity(ad);
        InputDispenser idisp = new CoreInputDispenser(a);
        ActionDispenser adisp = new CoreActionDispenser(a);
        OutputDispenser tdisp = CoreServices.getOutputDispenser(a).orElse(null);
        MotorDispenser<?> mdisp = new CoreMotorDispenser(a, idisp, adisp, tdisp);
        a.setActionDispenserDelegate(adisp);
        a.setInputDispenserDelegate(idisp);
        a.setMotorDispenserDelegate(mdisp);

        ActivityExecutor ae = new ActivityExecutor(a, "test-new-executor");
        ad.setThreads(5);
        ae.startActivity();

        int[] speeds = new int[]{1, 2000, 5, 2000, 2, 2000};
        for (int offset = 0; offset < speeds.length; offset += 2) {
            int threadTarget = speeds[offset];
            int threadTime = speeds[offset + 1];
            logger.info(() -> "Setting thread level to " + threadTarget + " for " + threadTime + " seconds.");
            ad.setThreads(threadTarget);
            try {
                Thread.sleep(threadTime);
            } catch (InterruptedException ignored) {
            }
        }
        ad.setThreads(0);

    }

    private MotorDispenser<?> getActivityMotorFactory(final ActivityDef ad, Action lc, final Input ls) {
        MotorDispenser<?> cmf = new MotorDispenser<>() {
            @Override
            public Motor getMotor(ActivityDef activityDef, int slotId) {
                Activity activity = new SimpleActivity(activityDef);
                Motor<?> cm = new CoreMotor(activity, slotId, ls);
                cm.setAction(lc);
                return cm;
            }
        };
        return cmf;
    }

    private SyncAction motorActionDelay(final long delay) {
        SyncAction consumer = new SyncAction() {
            @Override
            public int runCycle(long cycle) {
                System.out.println("consuming " + cycle + ", delaying:" + delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {
                }
                return 0;
            }
        };
        return consumer;
    }

    private static class DelayedInitActivity extends SimpleActivity {
        private static final Logger logger = LogManager.getLogger(DelayedInitActivity.class);

        public DelayedInitActivity(ActivityDef activityDef) {
            super(activityDef);
        }

        @Override
        public void initActivity() {
            Integer initdelay = activityDef.getParams().getOptionalInteger("initdelay").orElse(0);
            logger.info(() -> "delaying for " + initdelay);
            try {
                Thread.sleep(initdelay);
            } catch (InterruptedException ignored) {
            }
            logger.info(() -> "delayed for " + initdelay);
        }
    }
}
