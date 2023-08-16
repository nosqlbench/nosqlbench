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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityapi.core.RunState;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Holds the state of a slot, allows only valid transitions, and shares the
 * slot state as
 */
public class MotorState implements Supplier<RunState> {
    private final static Logger logger = LogManager.getLogger("MOTORS");
    private final AtomicReference<RunState> atomicState = new AtomicReference<>(RunState.Uninitialized);
    private final long slotId;
    private final RunStateTally tally;

    public MotorState(long slotId, RunStateTally tally) {
        this.slotId = slotId;
        this.tally = tally;
        tally.add(atomicState.get());
    }

    public RunState get() {
        return atomicState.get();
    }

    /**
     * This is how you share the current slot state most directly, but it has a caveat. By sharing the
     * slot state in this way, you allow external changes. You should only use this method to share slot
     * state for observers.
     * @return an atomic reference for SlotState
     */
    public AtomicReference<RunState> getAtomicSlotState() {
        return atomicState;
    }

    /**
     * <p>Transition the thread slot to a new state. only accepting valid transitions.</p>
     * <p>The valid slot states will be moved to a data type eventually, simplifying this method.</p>
     *
     * @param to The next SlotState for this thread/slot/motor
     */
    public synchronized void enterState(RunState to) {
        RunState from = atomicState.get();
        if (!from.canTransitionTo(to)) {
            throw new RuntimeException("Invalid transition from " + from + " to " + to);
        }
        while (!atomicState.compareAndSet(from, to)) {
            logger.trace(() -> "retrying transition from:" + from + " to:" + to);
        }
        tally.change(from,to);
        logger.trace(() -> "TRANSITION[" + slotId + "]: " + from + " ==> " + to);
    }

    public void removeState() {
        logger.trace(() -> "Removing motor state " + atomicState.get());
        tally.remove(atomicState.get());
    }
}
