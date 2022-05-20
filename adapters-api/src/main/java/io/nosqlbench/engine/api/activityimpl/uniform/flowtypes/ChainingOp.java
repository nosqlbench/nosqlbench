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


import java.util.function.Function;

/**
 * Run a function on the current cached result and replace it
 * with the result of the function. Functions are one way of invoking
 * logic within a cycle. However, they are not intended to stand alone.
 * A CycleFunction must always have an input to work on. This input is
 * provided by a Supplier as optionally implemented by an Op
 *
 * @param <I> Some input type.
 * @param <O> Some output type.
 */
public interface ChainingOp<I,O> extends Op, Function<I,O> {
    @Override
    O apply(I i);
}
