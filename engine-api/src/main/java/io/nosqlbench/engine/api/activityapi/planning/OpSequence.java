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

package io.nosqlbench.engine.api.activityapi.planning;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * An OpSequence provides fast access to a set of operations in a specific
 * order.
 *
 * @param <T> The type of element which is to be sequenced
 */
public interface OpSequence<T> extends LongFunction<T> {

    /**
     * Get the list of individual operations which could be returned by {@link #apply(long)}.
     * @return A {@link List} of T
     */
    List<T> getOps();

    /**
     * Get the integer sequence that is used to index into the operations.
     * @return an offset pointer array in int[] form
     */
    int[] getSequence();

    /**
     * Map this OpSequence to another type of OpSequence.
     * @param func The transformation function from this to another type
     * @param <U> The target type of the transformation.
     * @return A new OpSequence of type U
     */
    <U> OpSequence<U> transform(Function<T, U> func);

}
