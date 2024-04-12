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

package io.nosqlbench.virtdata.api.processors;

/**
 * Statically defined names which need to be visible for refactoring
 * and safe source manipulation.
 */
public class ProcessorClassNames {
    public final static String ThreadSafeMapper =
            "io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper";
    public final static String PerThreadMapper =
            "io.nosqlbench.virtdata.api.annotations.PerThreadMapper";
}
