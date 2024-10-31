/*
 * Copyright (c) nosqlbench
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

/**
 * <PRE>{@code
 * errored_at > 0   -> ERROR
 * started_at == 0   -> STARTING
 * <p>
 * <p>
 * <p>
 * started_at > closed_at
 * STARTING
 * closed_at > started_at
 * RUNNING
 * teardown_at > closed_at
 * STOPPING
 * teardown_at
 * STOPPED
 * stopped_at
 * }</pre>
 */
public enum NBInvokableState {
    /**
     * The component exists in some state but has not completed initialization / construction
     */
    STARTING,
    /**
     * The component has completed initialization and is presumed to be running
     */
    RUNNING,
    /**
     * The component has begun closing down, which means unwinding/closing any child components
     */
    CLOSING,
    /**
     * The component has completed closing down, including its teardown logic
     */
    STOPPED,
    /**
     * There was an error
     */
    ERRORED;

}
