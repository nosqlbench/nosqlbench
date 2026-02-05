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

package io.nosqlbench.virtdata.core.templates;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * If an op implements VariableCapture, then it is known to be able to
 * extract variables from its result. Generally speaking, this should
 * be implemented within an Op according to the standard format
 * of {@link ParsedTemplateString#getCaptures()}. Any op implementing
 * this should use the interface below to support interop between adapters
 * and to allow for auto documentation tha the feature is supported for
 * a given adapter.
 */

/// Any type implementing [[UniformVariableCapture]] will allow a caller to extract a map of names and values from it.
/// The implementor of [[#initCaptureF(List)]] is responsible for providing a function to extract dynamic values.
/// This function will be cached in the runtime and used when needed.
///
/// In cases where a type assertion is provided, the values captured dynamically will be subject to an
/// additional phase of type validation and coercion. If required types are not compatible, a
/// variable capture error should be thrown.
public interface UniformVariableCapture<RESULT> {
    Function<RESULT,Map<String,?>> initCaptureF(CapturePoints<RESULT> points);
}
