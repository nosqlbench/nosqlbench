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
 * An InputDispenser is created for each Activity instance within a scenario.
 * When a thread is created, the motor and its input and action instances are resolved.
 * The InputDispenser is responsible for choosing how the input is resolved,
 * whether that is a shared thread-safe input or an input per slot.
 */
public interface InputDispenser {

    /**
     * Resolve (find or create) an Input instance for the slot specified.
     * The input is not required to be per-slot (per-thread), but any shared inputs must be thread safe.
     * @param slot The numbered slot within the activity instance for this action.
     * @return A new or cached Input for the specified slot.
     */
    Input getInput(long slot);
}
