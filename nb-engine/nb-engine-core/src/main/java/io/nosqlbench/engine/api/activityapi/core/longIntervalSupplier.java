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

public interface longIntervalSupplier {
    /**
     * Get the next interval to be consumed by the caller, where the
     * first value is the returned value, and the last value is
     * one less than the first value plus the stride.
     * @param stride How many values to request
     * @return the base value of the interval to consume
     */
    long getCycleInterval(int stride);
}
