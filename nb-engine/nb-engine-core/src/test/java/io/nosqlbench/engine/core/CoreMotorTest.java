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
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.Motor;
import io.nosqlbench.engine.api.activityapi.core.StrideAction;
import io.nosqlbench.engine.api.activityimpl.motor.StrideMotor;
import io.nosqlbench.engine.core.fortesting.BlockingSegmentInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
public class CoreMotorTest {

    @Test
    public void testBasicActivityMotor() {
        final Activity activity = new Activity(
            new TestComponent("testing", "coremotor"),
            ActivityDef.parseActivityDef("alias=foo")
        );
        final BlockingSegmentInput lockstepper = new BlockingSegmentInput();
        final AtomicLong observableAction = new AtomicLong(-3L);
        final Motor cm = new StrideMotor(activity, 5L, lockstepper,
            new StrideAction() {
                @Override
                public int runCycle(long cyclenum) {
                    observableAction.set(cyclenum);
                    return 0;
                }
            }, null);
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
        final AtomicLongArray ary = new AtomicLongArray(10);
        final Motor cm1 = new StrideMotor(activity, 1L, lockstepper,
            new StrideAction() {
                @Override
                public int runCycle(long cyclenum) {
                    ary.set((int) (cyclenum - 11), cyclenum);
                    return 0;
                }
            }, null);

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
