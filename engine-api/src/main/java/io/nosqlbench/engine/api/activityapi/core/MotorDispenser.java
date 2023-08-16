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
package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.api.engine.activityimpl.ActivityDef;

/**
 * A MotorDispenser is created for each Activity instance within a scenario.
 * When a thread is created, the motor and its input and action instances are resolved.
 * The MotorDispenser is responsible for choosing how the motor is resolved,
 * whether that is a shared thread-safe motor or, more conventionally, a separate motor per slot.
 */
public interface MotorDispenser<T> {

    /**
     * Resolve (find or create) a Motor instance for the slot specified.
     * The motor is not required to be per-slot (per-thread), but any shared inputs motors be thread safe.
     *
     * @param activityDef the ActivityDef which will be used to parameterize the returned motor
     * @param slot The numbered slot within the activity instance for this motor
     * @return A new or cached Motor for the specified slot.
     */
    Motor<T> getMotor(ActivityDef activityDef, int slot);
}
