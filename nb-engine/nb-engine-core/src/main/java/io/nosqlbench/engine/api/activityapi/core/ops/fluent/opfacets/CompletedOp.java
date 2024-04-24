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

package io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;

/**
 * A CompletedOp can be any of
 * <UL>
 *     <LI>{@link SucceededOp}</LI>
 *     <LI>{@link FailedOp}</LI>
 *     <LI>{@link SkippedOp}</LI>
 * </UL>
 *
 * It may be necessary to downcast a completed Op in order to
 * get more contextual details from it.
 *
 * @param <D> The type of delegate needed for the
 *           implementing protocol
 */
public interface CompletedOp<D> extends Payload<D>, CycleResult {
    long getStartedAtNanos();
    long getServiceTimeNanos();
    long getResponseTimeNanos();
}
