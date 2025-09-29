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

import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.Motor;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotor;
import io.nosqlbench.engine.core.fortesting.BlockingSegmentInput;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreMotorTest {

    @Test
    public void testBasicActivityMotor() {
        final Activity activity = new Activity(
            new TestComponent("testing", "coremotor"),
            ActivityDef.parseActivityDef("alias=foo")
        );
        final BlockingSegmentInput lockstepper = new BlockingSegmentInput();
        final Motor cm = new CoreMotor(activity, 5L, lockstepper);
        final AtomicLong observableAction = new AtomicLong(-3L);
        cm.setAction(this.getTestConsumer(observableAction));
        final Thread t = new Thread(cm);
        t.setName("TestMotor");
        t.start();
        try {
            Thread.sleep(1000);  // allow action time to be waiting in monitor for test fixture
        } catch (final InterruptedException ignored) {
        }

        lockstepper.publishSegment(5L);
        final boolean result = this.awaitCondition(atomicInteger -> 5L == atomicInteger.get(), observableAction, 5000, 100);
        assertThat(observableAction.get()).isEqualTo(5L);
    }

    @Test
    public void testIteratorStride() {
        Activity activity = new Activity(TestComponent.INSTANCE, "stride=3");
        final BlockingSegmentInput lockstepper = new BlockingSegmentInput();
        final Motor cm1 = new CoreMotor(activity, 1L, lockstepper);
        final AtomicLongArray ary = new AtomicLongArray(10);
        final Action a1 = this.getTestArrayConsumer(ary);
        cm1.setAction(a1);

        final Thread t1 = new Thread(cm1);
        t1.setName("cm1");
        t1.start();
        try {
            Thread.sleep(500); // allow action time to be waiting in monitor for test fixture
        } catch (final InterruptedException ignored) {
        }

        lockstepper.publishSegment(11L, 12L, 13L);

        final boolean result = this.awaitAryCondition(ala -> 13L == ala.get(2), ary, 5000, 100);
        assertThat(ary.get(0)).isEqualTo(11L);
        assertThat(ary.get(1)).isEqualTo(12L);
        assertThat(ary.get(2)).isEqualTo(13L);
        assertThat(ary.get(3)).isEqualTo(0L);

    }

    private SyncAction getTestArrayConsumer(AtomicLongArray ary) {
        return new SyncAction() {
            private int offset;

            @Override
            public int runCycle(final long cycle) {
                ary.set(this.offset, cycle);
                this.offset++;
                return 0;
            }
        };
    }

    private SyncAction getTestConsumer(AtomicLong atomicLong) {
        return new SyncAction() {
            @Override
            public int runCycle(final long cycle) {
                atomicLong.set(cycle);
                return 0;
            }
        };
    }


    private boolean awaitAryCondition(final Predicate<AtomicLongArray> atomicLongAryPredicate, final AtomicLongArray ary, final long millis, final long retry) {
        final long start = System.currentTimeMillis();
        long now = start;
        while (now < (start + millis)) {
            final boolean result = atomicLongAryPredicate.test(ary);
            if (result) return true;
            try {
                Thread.sleep(retry);
            } catch (final InterruptedException ignored) {
            }
            now = System.currentTimeMillis();
        }
        return false;
    }

    private boolean awaitCondition(final Predicate<AtomicLong> atomicPredicate, final AtomicLong atomicInteger, final long millis, final long retry) {
        final long start = System.currentTimeMillis();
        long now = start;
        while (now < (start + millis)) {
            final boolean result = atomicPredicate.test(atomicInteger);
            if (result) return true;
            try {
                Thread.sleep(retry);
            } catch (final InterruptedException ignored) {
            }
            now = System.currentTimeMillis();
        }
        return false;
    }
}
