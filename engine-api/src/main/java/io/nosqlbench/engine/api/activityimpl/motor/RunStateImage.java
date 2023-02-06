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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A value type which encodes the atomic state of a RunState tally.
 */
public class RunStateImage {
    private final static Logger logger = LogManager.getLogger("TALLY");

    private final int[] counts = new int[RunState.values().length];
    private final boolean timedout;

    public RunStateImage(int[] counts, boolean timedout) {
        System.arraycopy(counts, 0, this.counts, 0, counts.length);
        this.timedout = timedout;
    }

    public boolean isTimeout() {
        return this.timedout;
    }

    public boolean is(RunState runState) {
        return counts[runState.ordinal()]>0;
    }

    public boolean isOnly(RunState runState) {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i]>0 && i!=runState.ordinal()) {
                return false;
            }
        }
        return true;
    }

    public RunState getMaxState() {
        for (int ord = counts.length-1; ord >= 0; ord--) {
            if (counts[ord]>0) {
                return RunState.values()[ord];
            }
        }
        throw new RuntimeException("There were zero states, so max state is undefined");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (RunState runState : RunState.values()) {
            sb.append(runState.getCode()).append(":").append(counts[runState.ordinal()]).append(" ");
        }
        return sb.toString();
    }

}
