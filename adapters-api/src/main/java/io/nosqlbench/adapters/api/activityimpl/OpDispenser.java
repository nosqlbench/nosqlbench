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

package io.nosqlbench.adapters.api.activityimpl;

import io.nosqlbench.adapters.api.evalcontext.CycleFunction;

import java.util.function.LongFunction;

/**
 * <p>
 * <H2>Synopsis</H2>
 * An OpDispenser is responsible for mapping a cycle number into
 * an executable operation. This is where <i>Op Synthesis</i> occurs
 * in NoSQLBench.</p>
 * <hr/>
 * <p>
 * <H2>BaseOpDispenser</H2>
 * </p>
 * Some common behaviors which are intended to be portable across all op
 * dispenser types are implemented in {@link BaseOpDispenser}. It is
 * <em>strongly</em> recommended that you use this as your base type when
 * implementing op dispensers.
 * <p>
 * <H2>Concepts</H2>
 * Op Synthesis is the process of building a specific executable
 * operation for some (low level driver) API by combining the
 * static and dynamic elements of the operation together.
 * In most cases, implementations of OpDispenser will be constructed
 * within the logic of an {@link OpMapper} which is responsible for
 * determining the type of OpDispenser to use as associated with a specific
 * type &lt;T&gt;. The OpMapper is called for each type of operation
 * that is active during activity initialization. It's primary responsibility
 * is figuring out what types of {@link OpDispenser}s to create based
 * on the op templates provided by users. Once the activity is initialized,
 * a set of op dispensers is held as live dispensers to use as needed
 * to synthesize new operations from generated data in real time.
 * </p>
 *
 * <hr/>
 * <h2>Implementation Strategy</h2>
 * <p>OpDispenser implementations are intended to be implemented
 * for each type of distinct operation that is supported by a
 * DriverAdapter.
 * That is not to say that an OpDispenser can't be responsible for
 * producing multiple types of operations. Operations which are similar
 * in what they need and how they are constructed make sense to be implemented
 * in the same op dispenser. Those which need different construction
 * logic or fundamentally different types of field values should be implemented
 * separately. The rule of thumb is to ensure that op construction patterns
 * are easy to understand at the mapping level ({@link OpMapper}),
 * and streamlined for fast execution at the synthesis level ({@link OpDispenser}).
 *
 * @param <T> The parameter type of the actual operation which will be used
 *            to hold all the details for executing an operation,
 *            something that implements {@link Runnable}.
 */
public interface OpDispenser<T> extends LongFunction<T>, OpResultTracker {

    /**
     * The apply method in an op dispenser should do all the work of
     * creating an operation that is executable by some other caller.
     * The value produced by the apply method should not require
     * additional processing if a caller wants to execute the operation
     * multiple times, as for retries.
     *
     * @param value The cycle number which serves as the seed for any
     *              generated op fields to be bound into an operation.
     * @return an executable operation
     */

    T apply(long value);

    CycleFunction<Boolean> getVerifier();

}
