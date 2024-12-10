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

import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;

import java.util.function.LongFunction;

/// The [CycleOp] is the core interface for any executable operation within
/// the NoSQLBench runtime. When [#apply(long)] is called, the long value
/// is the value of a specific cycle. It is effectively `cycleop.apply(cycle) -> RESULT`.
///
/// [CycleOp]s may or may not actually use the long input. Generally speaking,
/// once you have an instance of a cycle op, you have already synthesized all
/// concrete values needed to instantiate it. These values are derived from
/// the cycle within the op dispensing layer, thus the [CycleOp] should not need
/// them again to define the core operation. However, it is often helpful
/// to have the cycle value for debugging, diagnostics, or other instrumentation,
/// particularly in cases where special error handling is needed. In some rare
/// cases, special dispensers may use the value to reconstruct determinstic op
/// data for the purposes of troubleshooting or similar.
///
/// A given [CycleOp] instance should execute the exact same operation if called again.
/// This is used to _retry_ operations in some cases. Some operations which may be
/// spawned as a result of previous operations may never use the cycle op value, as the
/// fields and behavior of those secondary operations may be fully determined by the
/// results or fields of the previous operation. This may be the case for linearized
/// operations which, for example, read further data from a data source as a client-side
/// join.
///
/// ## Designer Notes
///
/// If you are using the value in this call to select a specific type of behavior, i.e. among a variety of
/// operation types, it is very likely a candidate for factoring into separate op implementations. In general,
/// you should move as much of the initialization logic forward as possible, to keep op synthesis fast.
///
/// If you derive from [CycleOp] to create subtypes within your own
///  [io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter] implementation, it is better to
/// keep the [RESULT] generic parameter within the arity of the derived type. For example, a
/// derived
/// ```
/// public ... MyCycleOp<? extends MyBaseRequestType, RESULT extends MyBaseResultType>
///  extends CycleOp<RESULT>
/// ```
/// is much more understandable and reusable than
/// ```
/// public ... MyCycleOp<? extends MyBaseRequestType>
///  extends CycleOp<Object> # confusing!
/// ```
/// since the latter version replaces the generic result type with some other adapter-specific type.
/// At least in the first case, a clear specialization is carved out for a new generic parameter
/// without hiding the original result parameter.
///
public interface CycleOp<RESULT> extends LongFunction<RESULT> {
    /**
     * <p>Run an action for the given cycle.</p>
     *
     * @param value
     *     The cycle value for which an operation is run
     */
    @Override
    RESULT apply(long value);


}
