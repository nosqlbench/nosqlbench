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

package io.nosqlbench.activitytype.diag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongToIntFunction;

public class DiagOpData {

    private final String description;
    private final List<String> diaglog = new ArrayList<>();

    private LongToIntFunction resultFunc;
    private long simulatedDelayNanos;

    public DiagOpData(String description) {
        this.description = description;
    }

    /**
     * If this function is provided, the result will be set to the value of the
     * evaluated function with the op cycle.
     *
     * This is known as "resultfunc" in parameter space.
     *
     * The function must be thread-safe.
     *
     * @param resultFunc A function to map the cycle to the result value
     * @return this, for method chaining
     */
    public DiagOpData withResultFunction(LongToIntFunction resultFunc) {
        this.resultFunc = resultFunc;
        return this;
    }

    /**
     * If this function is provided, the completion of the operation will be
     * delayed until the system nanotime is at least the op start time in
     * addition to the provided delay.
     *
     * This is controlled as "delayfunc" in parameter space.
     *
     * @param simulatedDelayNanos The amount of nanos ensure as a minimum
     *                            of processing time for this op
     */
    public DiagOpData setSimulatedDelayNanos(long simulatedDelayNanos) {
        this.simulatedDelayNanos = simulatedDelayNanos;
        return this;
    }

    public long getSimulatedDelayNanos() {
        return simulatedDelayNanos;
    }

    @Override
    public String toString() {
        return super.toString() + ", description:'" + description;
    }
    public String getDescription() {
        return description;
    }
    public void log(String logline) {
        this.diaglog.add(logline);
    }
    public List<String> getDiagLog() {
        return diaglog;
    }

}
