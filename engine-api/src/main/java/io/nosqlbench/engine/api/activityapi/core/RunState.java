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

package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.engine.api.activityimpl.MotorState;

/**
 * <P>This enum indicates the state of a thread within an activity. The state is kept in an atomic
 * register. Ownership of state changes is shared between a supervising thread and a managed thread.
 * Both can make changes to the state.</P>
 *
 * <P>These states are ordered such that the highest ordinal state represents the most significant
 * aggregate state of all motors. That is, if any has errored, then the other states do not matter.
 * If any is finished, then stopped motors don't matter, and so on. This makes summarizing aggregate
 * state simply a matter of ordering.</P>
 *
 */
public enum RunState {

    /**
     * Initial state after creation of a motor. This is the initial state upon instantiation of a motor, before
     * it is called on to do any active logic besides what occurs in the constructor.
     */
    Uninitialized("i⌀"),

    /**
     * A thread has been invoked, but is initializing and preparing for its main control loop.
     * This is signaled <EM>by the motor</EM> after {@link Runnable#run}, but before entering the main processing
     * loop.
     */
    Starting("s⏫"),

    /**
     * A thread is iterating within the main control loop.
     * This is signaled <EM>by the motor</EM> once initialization in the main loop is complete and immediately
     * before it enters it's main processing loop.
     */
    Running("R\u23F5"),

    /**
     * <P>The thread has been requested to stop. This can be set by a managing thread which is not the
     * motor thread, or by the motor thread. In either case, the motor thread is required to observe changes to this and initiate shutdown.</P>
     */
    Stopping("s⏬"),

    /**
     * The thread has stopped. This should only be set by the motor. This state will only be visible
     * to signaling mechanisms so long as the motor is still managed.
     *
     * <P>NOTE: When a motor is stopped or finished, its state will remain visible in state tracking until
     * {@link Motor#getState()}.{@link MotorState#removeState()} is called.</P>
     */
    Stopped("e\u23F9"),

    /**
     * <P>A thread has exhausted its supply of values on the input (AKA cycles), thus has completed its work.
     * This is signaled upon a short read of the input <EM>by the motor</EM>.</P>
     *
     * <P>NOTE: When a motor is stopped or finished, its state will remain visible in state tracking until
     * {@link Motor#getState()}.{@link MotorState#removeState()} is called.</P>
     */
    Finished("F⏯"),

    /**
     * If a motor has seen an exception, it goes into errored state before propagating the error.
     */
    Errored("E⚠");

    private final String runcode;

    RunState(String runcode) {
        this.runcode = runcode;
    }

    public String getCode() {
        return this.runcode;
    }

    /**
     * @param target The target state
     * @return true if the current state is allowed to transition to the target state
     */
    public boolean canTransitionTo(RunState target) {
        return switch (this) {
            default -> false; // A motor was just created. This is its initial state.
            case Uninitialized, Stopped -> (target == Starting);
            case Starting -> switch (target) { // a motor has indicated that is in the run() method
                case Running, Finished, Errored -> true;// a motor has exhausted its input, and has declined to go into started mode
                default -> false;
            };
            case Running -> switch (target) { // A request was made to stop the motor before it finished
                case Stopping, Finished, Errored -> true;// A motor has exhausted its input, and is finished with its work
                default -> false;
            };
            case Stopping -> (target == Stopped||target==Finished); // A motor was stopped by request before exhausting input
            case Finished -> (target == Running); // A motor was restarted?
            case Errored -> target==Errored;
        };

    }

}
