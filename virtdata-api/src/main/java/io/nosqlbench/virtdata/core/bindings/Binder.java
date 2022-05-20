package io.nosqlbench.virtdata.core.bindings;

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

/***
 * A Binder is a type that knows how to return a result object given a long value
 * to bind mapped values with.
 * @param <R> The resulting object type
 */
public interface Binder<R> extends LongFunction<R> {
    /**
     * Bind values derived from a long to some object, returning an object type R
     * @param value a long input value
     * @return an R
     */
    R bind(long value);

    @Override
    default R apply(long value) {
        return bind(value);
    }
}
