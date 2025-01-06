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

/// This interface allows an op dispenser or related type to extract a set of field values from
/// a result of [RESULT] type. Since the result type and thus the logic for reading the values out
/// will vary by protocol or API, this optional behavior must be provided by adapter or
/// dispenser-specific implementations.
///
/// A type implementing [UniformVariableCapture] is able to provide a mapping
/// function from the [RESULT] type to the neutral [Map] of [String] to [Object] form.
///
/// In some cases, it may be possible for the variable capture logic to be pre-baked around a static
/// set of fields and API calls, such as for a stable table schema. In this mode, the logic of
/// [#captureF(CapturePoints)] will be about traversing a known result manifest and constructing a
/// lambda which can reliably extract fields from the result type without further interpretation.
/// This is obviously the most optimal path for result types which are idiomatically regular in
/// structure. This can be thought of as a "pre-compiled" mode for variable capture. (This is close
/// to the truth, actually, due to effective JIT optimization of lambdas.)
///
/// In other cases, it may not be possible due to the result type having a varying structure,
/// optional fields, etc. In these cases, the logic of the [#captureF(CapturePoints)] method is
/// to simply return an interpretive function which can walk variant result structure. This can
/// be thought of as an "interpreted" mode for variable capture, at least to the extent that the
/// result structure will have to be intepreted anew for each instance.
///
/// It is not the responsibility of the [#captureF(CapturePoints)] implementation to enforce
/// types or projected names. A future version will likely constrain the view of CapturePoint to
/// only include the required field names for extraction. Another layer in the NB runtime is
/// responsible for asserting assignability to the cast types indicated by [CapturePoint#asCast]
/// and to the projected names indicated by [CapturePoint#asName].
public interface UniformVariableCapture<RESULT> {
    /// Return a function which can extract the variables from the [RESULT] type, specified
    /// by the given [CapturePoint#sourceName]s.
    Function<RESULT,Map<String,?>> captureF(CapturePoints<RESULT> points);
}
