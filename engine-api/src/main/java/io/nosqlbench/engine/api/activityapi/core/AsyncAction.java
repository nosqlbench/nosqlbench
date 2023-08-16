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

import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;

import java.util.function.LongFunction;

/**
 * An AsyncAction allows an activity type to implement asynchronous
 * operations within each thread.
 */
public interface AsyncAction<D> extends Action {

    LongFunction<D> getOpInitFunction();

    /**
     * THIS DOCUMENTATION IS LIKELY OUT OF DATE
     *
     * The responsibility for tracking async pending against concurrency limits,
     * including signaling for thread state, has been moved into the async
     * event loop of the core motor. If this experiment holds, then the docs
     * here must be rewritten to be accurate for that approach.
     **
     *
     * Enqueue a cycle to be executed by the action. This method should block unless
     * or until the action accepts the cycle to be processed.
     * This method is not allowed to reject a cycle. If it is unable to accept the
     * cycle for any reason, it must throw an exception.
     *
     * Since the action implementation is presumed to be running some externally
     * asynchronous process to support the action, it is up to the action itself
     * to control when to block enqueueing. If the action is not actually asynchronous,
     * then it may need to do downstream processing in order to open room in its
     * concurrency limits for the new cycle.
     *
     * Each action implementation is responsible for tracking and controlling
     * its own limits of concurrency. The {@link BaseAsyncAction} base class is a
     * convenient starting point for such implementations.
     *
     * If the action is known to have additional open slots for an operations to
     * be started (according to the configured concurrency limits),
     * then it can signal such by returning true from this method.
     *
     * @param opc The op context that holds state for this operation
     * @return true, if the action is ready immediately for another operation
     */
    boolean enqueue(TrackedOp<D> opc);

}
