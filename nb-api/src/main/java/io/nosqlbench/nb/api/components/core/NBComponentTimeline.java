/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.nb.api.components.core;

public interface NBComponentTimeline {

    NBInvokableState getComponentState();
    /**
     * This will be 0L if the component hasn't fully started, else it will be
     * the {@link System#nanoTime()} of when the component entered its constructor
     * @return nanosecond time of component construction
     */
    long nanosof_start();

    /**
     * This will be 0L if the component hasn't began the process of closing down.
     * @return nanosecond time of invoking {@link NBBaseComponent#close()}
     */
    long nanosof_close();

    /**
     * This will be 0L if the component hasn't completed teardown. Otherwise it will be
     * the {@link System#nanoTime()} when the base teardown logic in the component has completed.
     * For this reason, it is imperative that any overrides to {@link NBBaseComponent#teardown()}
     * are called, and called last in the overridden teardown method.
     * @return nanosecond time of teardown completion
     */
    long nanosof_teardown();

    /**
     * This will be 0L if the component hasn't logged an error. Otherwise it will be
     * the {@link System#nanoTime()} of when the error was reported.
     * @return nanosecond time of the error
     */
    long nanosof_error();
}
