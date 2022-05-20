package io.nosqlbench.engine.api.activityimpl.uniform.flowtypes;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.function.LongFunction;

/**
 * A CycleRunnable is simply a variation of a Runnable type.
 * The main difference is that it is supplied with the cycle
 * as input.
 */
public interface CycleOp<T> extends Op, LongFunction<T> {
//    /**
//     * <p>Run an action for the given cycle. The cycle is provided for anecdotal
//     * usage such as logging and debugging. It is valid to use the cycle value in these places,
//     * but you should not use it to determine the logic of what is run. The mechanism
//     * for doing this is provided in {@link io.nosqlbench.engine.api.activityimpl.OpMapper}
//     * and {@link io.nosqlbench.engine.api.activityimpl.OpDispenser} types.</p>
//     *
//     *
//     * @param cycle The cycle value for which an operation is run
//     */
////     * This method should do the same thing that {@link #apply(long)} does, except that
////     * there is no need to prepare or return a result. This is the form that will be called
////     * if there is no chaining operation to consume the result of this operation.
//    void accept(long cycle);

    /**
     * <p>Run an action for the given cycle. The cycle
     * value is only to be used for anecdotal presentation. This form is called
     * when there is a chaining operation which will do something with this result.</p>
     * @param value The cycle value for which an operation is run
     * @return A result which is the native result type for the underlying driver.
     */
    @Override
    T apply(long value);


}
