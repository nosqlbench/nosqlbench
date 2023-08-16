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

import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;

import java.util.function.LongFunction;

/**
 * An OpSource provides an Op for a given long value.
 * OpSources are expected to be deterministic with respect to inputs.
 *
 * @param <T>
 */
public interface OpSource<T> extends LongFunction<T> {

    static <O extends Op> OpSource<O> of(OpSequence<OpDispenser<? extends O>> seq) {
        return (long l) -> seq.apply(l).apply(l);
    }

    /**
     * Get the next operation for the given long value. This is simply
     * the offset indicated by the offset sequence array at a modulo
     * position.
     *
     * @param selector the long value that determines the next op
     * @return An op of type T
     */
    T get(long selector);

    @Override
    default T apply(long value) {
        return get(value);
    }
}
