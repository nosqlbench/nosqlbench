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

import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;

import java.util.function.LongFunction;

/**
 * <H2>CycleOp: f(cycle) -> T</H2>
 * <p>A CycleOp of T is an operation which takes a long input value
 * and produces a value of type T. It is implemented as
 * {@link LongFunction} of T.</p>
 *
 * <P>This variant of {@link Op} has the ability to see the cycle
 * which was previously used to select the op implementation.</p>
 *
 * <p>It also has the ability to emit an value which can be seen a subsequent operation, if
 * and only if it is a {@link ChainingOp}s.</P>
 *
 * <h2>Designer Notes</h2>
 * <p>
 * If you are using the value in this call to select a specific type of behavior, it is very
 * likely a candidate for factoring into separate op implementations.
 * The {@link OpMapper}
 * and {@link OpDispenser} abstractions are meant to move
 * op type selection and scheduling to earlier in the activity.
 * </p>
 *
 */
public interface CycleOp<T> extends Op, LongFunction<T> {
    /**
     * <p>Run an action for the given cycle.</p>
     *
     * @param value The cycle value for which an operation is run
     * @return A result object which <em>may</em> be used by a subsequent {@link ChainingOp}
     */
    @Override
    T apply(long value);


}
