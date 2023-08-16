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

package io.nosqlbench.engine.api.activityimpl.motor;

import io.nosqlbench.engine.api.activityapi.core.RunState;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RunStateTallyTest {

    volatile boolean awaited = false;
    volatile RunStateImage event = null;

    @BeforeEach
    public void setup() {
        awaited = false;
        event = null;
    }

    @Test
    @Order(1)
    public void testAwaitAny() {
        Thread.currentThread().setName("SETTER");

        RunStateTally tally = new RunStateTally();
        awaited = false;
        Thread waiter = new Thread(new Runnable() {
            @Override
            public void run() {
                event = tally.awaitAny(RunState.Running);
                awaited = true;
            }
        });
        waiter.setName("WAITER");
        waiter.setDaemon(true);
        waiter.start();

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isFalse();
        tally.add(RunState.Running);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }


        assertThat(event.is(RunState.Running)).isTrue();
        assertThat(event.isOnly(RunState.Running)).isTrue();

        assertThat(awaited).isTrue();
        assertThat(waiter.getState()).isNotEqualTo(Thread.State.RUNNABLE);
    }

    @Test
    @Order(2)
    public void testAwaitNoneOf() {
        Thread.currentThread().setName("SETTER");

        RunStateTally tally = new RunStateTally();
        tally.add(RunState.Uninitialized);
        tally.add(RunState.Stopped);
        awaited = false;
        Thread waiter = new Thread(new Runnable() {
            @Override
            public void run() {
                tally.awaitNoneOf(RunState.Stopped, RunState.Uninitialized);
                awaited = true;
            }
        });
        waiter.setName("WAITER");
        waiter.setDaemon(true);
        waiter.start();

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isFalse();
        tally.change(RunState.Stopped, RunState.Finished);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isFalse();
        tally.change(RunState.Uninitialized, RunState.Finished);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isTrue();
        assertThat(waiter.getState()).isNotEqualTo(Thread.State.RUNNABLE);

    }

    @Test
    @Order(3)
    public void testAwaitNoneOther() {
        Thread.currentThread().setName("SETTER");

        RunStateTally tally = new RunStateTally();
        tally.add(RunState.Uninitialized);
        tally.add(RunState.Running);
        awaited = false;
        Thread waiter = new Thread(new Runnable() {
            @Override
            public void run() {
                event = tally.awaitNoneOther(RunState.Stopped, RunState.Finished);
                awaited = true;
            }
        });
        waiter.setName("WAITER");
        waiter.setDaemon(true);
        waiter.start();

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isFalse();
        tally.change(RunState.Uninitialized, RunState.Finished);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isFalse();

        // Note that neither Stopped or Finished are required to be positive,
        // as long as all others are zero total.
        tally.remove(RunState.Running);

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isTrue();
        assertThat(waiter.getState()).isNotEqualTo(Thread.State.RUNNABLE);

    }

    @Test
    @Order(4)
    public void testAwaitNoneOtherTimedOut() {
        Thread.currentThread().setName("SETTER");

        RunStateTally tally = new RunStateTally();
        tally.add(RunState.Uninitialized);
        tally.add(RunState.Running);
        Thread waiter = new Thread(new Runnable() {
            @Override
            public void run() {
                event = tally.awaitNoneOther(1500, RunState.Stopped, RunState.Finished);
                awaited = true;
            }
        });
        waiter.setName("WAITER");
        waiter.setDaemon(true);
        waiter.start();

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        assertThat(awaited).isFalse();
        tally.change(RunState.Uninitialized, RunState.Finished);

        try {
            Thread.sleep(1500);
        } catch (Exception e) {
        }

//        try {
//            waiter.join();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        assertThat(event.isOnly(RunState.Errored)).isFalse();
        assertThat(waiter.getState()).isNotEqualTo(Thread.State.RUNNABLE);

    }


}
