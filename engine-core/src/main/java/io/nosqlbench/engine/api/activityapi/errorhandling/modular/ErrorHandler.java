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

package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

public interface ErrorHandler {
    /**
     * An error handler is able to do side-effect processing for a particular
     * error type, as well as modify the error detail that will be presented
     * back to the caller.
     *
     * @param t               Thrown from within the activity's action loop
     * @param cycle           The cycle which was executing when it ran
     * @param durationInNanos How long into an operation the error occured
     * @return true if the operation should be retried, assuming retries available
     */
    ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail);
}
