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
 * Provide the result size for an operation.
 */
public interface OpResultSize {
    /**
     * Provide the result size for an operation.
     * If this value is less than 0, it is disregarded by default, since some operations
     * don't make sense to have a result size, and a zero-value would skew the results
     * for other operations.
     */
    default long getResultSize() {
        return -1;
    }
}
