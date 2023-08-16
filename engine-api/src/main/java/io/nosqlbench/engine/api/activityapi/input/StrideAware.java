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

package io.nosqlbench.engine.api.activityapi.input;

/**
 * Any activity that implements StrideAware will be notified of the
 * exact interval that it is processing before the first cycle
 * is dispatched from the motor.
 */
public interface StrideAware {

    /**
     * Notify the StrideAware of the base cycle (inclusive) and the number
     * of cycles that it is about to be processing.
     * @param cycleStart The base cycle (inclusive)
     * @param cycleCount The number of cycles to be dispatched
     */
    void setInterval(long cycleStart, long cycleCount);
}
