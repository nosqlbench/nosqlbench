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

public interface SyncAction extends Action {

    /**
     * <p>Apply a work function to an input value, producing an int status code.</p>
     * The meaning of status codes is activity specific, however the values Integer.MIN_VALUE,
     * and Integer.MAX_VALUE are reserved.
     *
     * @param cycle a long input
     * @return an int status
     */
    default int runCycle(long cycle) {
        return (int) cycle % 100;
    }

}
