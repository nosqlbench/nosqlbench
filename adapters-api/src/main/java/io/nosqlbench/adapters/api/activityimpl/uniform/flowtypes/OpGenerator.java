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
 * <p>If an Op implements OpGenerator, then it will be asked for chained
 * operations that are secondary unless or until {@link #getNextOp()}}
 * returns null.</p>
 *
 * <p>If an Op *might* generate a secondary operation, then it should implement
 * this interface and simply return null in the case that there is none.</p>
 *
 * <p>If you need to run many operations after this, then you can keep the
 * state of these in the same Op implementation and simply keep returning
 * it until the work list is done. The same applies for structured op
 * generation, such as lists of lists or similar.</p>
 *
 */
public interface OpGenerator {
    Op getNextOp();
}
