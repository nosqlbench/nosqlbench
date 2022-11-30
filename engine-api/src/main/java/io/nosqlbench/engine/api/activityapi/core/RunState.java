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

public enum RunState {


    /**
     * Initial state after creation of this control
     */
    Uninitialized("i⌀"),

    /**
     * This thread has been queued to run, but hasn't signaled yet that it is full started
     * This must be set by the executor before executing the slot runnable
     */
    Starting("s⏫"),

    /**
     * This thread is running. This should only be set by the controlled thread
     */
    Running("R\u23F5"),

    /**
     * This thread has completed all of its activity, and will do no further work without new input
     */
    Finished("F⏯"),

    /**
     * The thread has been requested to stop. This says nothing of the internal state.
     */
    Stopping("s⏬"),

    /**
     * The thread has stopped. This should only be set by the controlled thread
     */
    Stopped("_\u23F9");

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
                case Running, Finished -> true;// a motor has exhausted its input, and has declined to go into started mode
                default -> false;
            };
            case Running -> switch (target) { // A request was made to stop the motor before it finished
                case Stopping, Finished -> true;// A motor has exhausted its input, and is finished with its work
                default -> false;
            };
            case Stopping -> (target == Stopped); // A motor was stopped by request before exhausting input
            case Finished -> (target == Running); // A motor was restarted?
        };

    }

}
