package io.nosqlbench.core;

import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.core.fortesting.BlockingSegmentInput;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotor;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/*
*   Copyright 2015 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
@Test
public class CoreMotorTest {

    @Test
    public void testBasicActivityMotor() {
        BlockingSegmentInput lockstepper = new BlockingSegmentInput();
        Activity activity = new SimpleActivity(ActivityDef.parseActivityDef("alias=foo"));
        Motor cm = new CoreMotor(activity, 5L, lockstepper);
        AtomicLong observableAction = new AtomicLong(-3L);
        cm.setAction(getTestConsumer(observableAction));
        cm.getSlotStateTracker().enterState(RunState.Starting);
        Thread t = new Thread(cm);
        t.setName("TestMotor");
        t.start();
        try {
            Thread.sleep(1000);  // allow action time to be waiting in monitor for test fixture
        } catch (InterruptedException ignored) {}

        lockstepper.publishSegment(5L);
        boolean result = awaitCondition(atomicInteger -> (atomicInteger.get()==5L),observableAction,5000,100);
        assertThat(observableAction.get()).isEqualTo(5L);
    }

    @Test
    public void testIteratorStride() {
        BlockingSegmentInput lockstepper = new BlockingSegmentInput();
        Motor cm1 = new CoreMotor(new SimpleActivity("stride=3"),1L, lockstepper);
        AtomicLongArray ary = new AtomicLongArray(10);
        Action a1 = getTestArrayConsumer(ary);
        cm1.setAction(a1);
        cm1.getSlotStateTracker().enterState(RunState.Starting);

        Thread t1 = new Thread(cm1);
        t1.setName("cm1");
        t1.start();
        try {
            Thread.sleep(500); // allow action time to be waiting in monitor for test fixture
        } catch (InterruptedException ignored) {}

        lockstepper.publishSegment(11L,12L,13L);

        boolean result = awaitAryCondition(ala -> (ala.get(2)==13L),ary,5000,100);
        assertThat(ary.get(0)).isEqualTo(11L);
        assertThat(ary.get(1)).isEqualTo(12L);
        assertThat(ary.get(2)).isEqualTo(13L);
        assertThat(ary.get(3)).isEqualTo(0L);

    }

    private SyncAction getTestArrayConsumer(final AtomicLongArray ary) {
        return new SyncAction() {
            private int offset=0;
            @Override
            public int runCycle(long value) {
                ary.set(offset++,value);
                return 0;
            }
        };
    }
    private SyncAction getTestConsumer(final AtomicLong atomicLong) {
        return new SyncAction() {
            @Override
            public int runCycle(long value) {
                atomicLong.set(value);
                return 0;
            }
        };
    }


    private boolean awaitAryCondition(Predicate<AtomicLongArray> atomicLongAryPredicate, AtomicLongArray ary, long millis, long retry) {
        long start = System.currentTimeMillis();
        long now=start;
        while (now < start + millis) {
            boolean result = atomicLongAryPredicate.test(ary);
            if (result) {
                return true;
            } else {
                try {
                    Thread.sleep(retry);
                } catch (InterruptedException ignored) {}
            }
            now = System.currentTimeMillis();
        }
        return false;
    }

    private boolean awaitCondition(Predicate<AtomicLong> atomicPredicate, AtomicLong atomicInteger, long millis, long retry) {
        long start = System.currentTimeMillis();
        long now=start;
        while (now < start + millis) {
            boolean result = atomicPredicate.test(atomicInteger);
            if (result) {
                return true;
            } else {
                try {
                    Thread.sleep(retry);
                } catch (InterruptedException ignored) {}
            }
            now = System.currentTimeMillis();
        }
        return false;
    }
}