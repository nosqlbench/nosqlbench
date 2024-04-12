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

/**
 * An ActionDispenser is created for each Activity instance within a scenario.
 * When a thread is created, the motor and its input and action instances are resolved.
 * The ActionDispenser is responsible for choosing how the action is resolved,
 * whether that is a shared thread-safe action or an action per slot.
 */
public interface ActionDispenser {

    /**
     * Resolve (find or create) an Action instance for the slot specified.
     * The action is not required to be per-slot (per-thread), but any shared actions must be thread safe.
     * @param slot The numbered slot within the activity instance for this action.
     * @return A new or cached Action for the specified slot.
     */
    Action getAction(int slot);
}
