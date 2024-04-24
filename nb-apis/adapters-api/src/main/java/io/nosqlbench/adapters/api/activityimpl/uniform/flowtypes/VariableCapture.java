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

import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;

import java.util.Map;

/**
 * If an op implements VariableCapture, then it is known to be able to
 * extract variables from its result. Generally speaking, this should
 * be implemented within an Op according to the standard format
 * of {@link ParsedTemplateString#getCaptures()}. Any op implementing
 * this should use the interface below to support interop between adapters
 * and to allow for auto documentation tha the feature is supported for
 * a given adapter.
 */
public interface VariableCapture {
    Map<String,?> capture();
}
