/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityapi.core;

public enum RunState {

    // Initial state after creation of this control
    Uninitialized("i⌀"),
    // This thread has been queued to run, but hasn't signaled yet that it is full started
    // This must be set by the executor before executing the slot runnable
    Starting("s⏫"),
    // This thread is running. This should only be set by the controlled thread
    Running("R\u23F5"),
    // This thread has completed all of its activity, and will do no further work without new input
    Finished("F⏯"),
    // The thread has been requested to stop. This says nothing of the internal state.
    Stopping("s⏬"),
    // The thread has stopped. This should only be set by the controlled thread
    Stopped("_\u23F9");

    private String runcode;

    RunState(String runcode) {
        this.runcode = runcode;
    }

    public String getCode() {
        return this.runcode;
    }

    public boolean canTransitionTo(RunState to) {
        switch (this) {
            default:
                return false;
            case Uninitialized: // A motor was just created. This is its initial state.
                switch (to) {
                    case Starting: // a motor has been reserved for an execution command
                        return true;
                    default:
                        return false;
                }
            case Starting:
                switch (to) {
                    case Running: // a motor has indicated that is in the run() method
                    case Finished: // a motor has exhausted its input, and has declined to go into started mode
                        return true;
                    default:
                        return false;
                }
            case Running:
                switch (to) {
                    case Stopping: // A request was made to stop the motor before it finished
                    case Finished: // A motor has exhausted its input, and is finished with its work
                        return true;
                    default:
                        return false;
                }
            case Stopping:
                switch (to) {
                    case Stopped: // A motor was stopped by request before exhausting input
                        return true;
                    default:
                        return false;
                }
            case Stopped:
                switch (to) {
                    case Running: // A motor was restarted after being stopped
                        return true;
                    default:
                        return false;
                }
            case Finished:
                switch (to) {
                    case Running: // A motor was restarted?
                        return true;
                    // not useful as of yet.
                    // Perhaps this will be allowed via explicit reset of input stream.
                    // If the input isn't reset, then trying to start a finished motor
                    // will cause it to short-circuit back to Finished state.
                    default:
                        return false;
                }
        }

    }

}
