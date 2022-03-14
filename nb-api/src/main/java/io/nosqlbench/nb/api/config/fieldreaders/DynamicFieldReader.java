/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.nb.api.config.fieldreaders;

import java.util.function.LongFunction;

/**
 * An interface which captures the semantics and patterns of
 * reading field values that are rendered functionally.
 * This interface is meant to help standardize the user
 * interfaces for reading configuration and fields across
 * the NB codebase.
 * See also {@link StaticFieldReader}
 * and {@link EnvironmentReader}
 */
public interface DynamicFieldReader {
    boolean isDynamic(String field);

    <T> T get(String field, long input);

    <V> LongFunction<V> getAsFunctionOr(String name, V defaultValue);
}
