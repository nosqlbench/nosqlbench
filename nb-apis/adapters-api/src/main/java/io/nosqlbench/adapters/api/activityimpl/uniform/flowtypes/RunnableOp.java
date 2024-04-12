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

/**
 * <H2>RunnableOp</H2>
 * <P>This is the simplest form of an executable operation in NoSQLBench.
 * It is simply an operation is run for side-effect only.</P>
 */
public interface RunnableOp extends Op, Runnable {

    /**
     * Invoke the operation. If you need to see the value of the current
     * cycle, then you can use {@link CycleOp} instead. If you need to
     * use a cached result of a previous operation, then you may need to
     * use {@link ChainingOp}.
     */
    @Override
    void run();
}
