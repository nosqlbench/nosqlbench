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

package io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes;

import java.util.function.Function;

/**
 * <H2>ChainingOp&lt;I,O&gt;: f(I) -> O&lt;I,O&gt;</H2>
 * <P>
 * Run a function on the current cached result in the current thread and replace it
 * with the result of the function. ChainingOps are one way of invoking
 * logic within a cycle. However, they are not intended to stand alone.
 * A ChainingOp must always have an input to work on,
 * provided by either a {@link CycleOp} OR <em>another</em> call to a {@link ChainingOp}</P>
 *
 * @param <I> Some input type, as determined by a previous {@link CycleOp} or {@link ChainingOp} on the same thread.
 * @param <O> Some output type.
 */
public interface ChainingOp<I, O> extends Op, Function<I, O> {

    /**
     * Transform a value from a previous action and provide the result for a subsequent action.
     *
     * @param lastResult object form a previous operation or action
     * @return a new result
     */
    @Override
    O apply(I lastResult);
}
