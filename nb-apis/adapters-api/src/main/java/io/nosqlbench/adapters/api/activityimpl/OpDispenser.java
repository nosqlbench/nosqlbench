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

package io.nosqlbench.adapters.api.activityimpl;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.evalctx.CycleFunction;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

///  ## Synopsis
///
/// An OpDispenser is responsible for producing an executable operation
/// for a given cycle number. OpDispenser's are __NOT__ responsible for determine what kind of
/// operation the user intended. These two roles are separated by design so that when an
/// OpDispenser is called, all of the work of figuring out what specific type of operations to run
/// has already been done. [OpDispenser]s are produced by [OpMapper]s. Thus the role of an OpMapper
///  is to construct a `cycle->op` function.
///
/// There is an important relationship between op templates which are interpreted by op mapping and
/// the executable operations which are produced by op dispensers. At a high level, the chain looks
/// like this:
/// ```
/// op template -> op mapper -> op dispenser -> executable op
///```
/// The types and roles associated with op synthesis are:
/// - [OpTemplate] - the raw op template data structure
/// - [ParsedOp] - an API around op template; a lambda construction kit
/// - [OpMapper] - interprets user intent by looking at the op template and constructs an
/// associated op dispenser
/// - [OpDispenser] - Applies a cycle number to a lambda to produce an executable op, and
/// keeps track of template-specific metrics.
/// - [CycleOp] - The base type of executable operations.
///
///  ----
///
///  ## BaseOpDispenser
///
///  Some common behaviors which are intended to be portable across all op
///  dispenser types are implemented in [BaseOpDispenser]. It is
///  __strongly__ recommended that you use this as your base type when
///  implementing op dispensers. (__refactoring will make this mandatory__)
///
///  ## Concepts
///
///  REWRITE BELOW
///  Op Synthesis is the process of building a specific executable
///  operation for some (low level driver) API by combining the
///  static and dynamic elements of the operation together.
///  In most cases, implementations of OpDispenser will be constructed
///  within the logic of an [OpMapper] which is responsible for
///  determining the type of OpDispenser to use as associated with a specific
///  type `<OPTYPE>`. The OpMapper is called for each op template
///  that is active (not excluded by tag filtering) during activity
///  initialization. It's primary responsibility is figuring out what types of
///  [OpDispenser]s to create based
///  on the op templates provided by users. Once the activity is initialized,
///  a set of op dispensers is held as live dispensers to use as needed
///  to synthesize new operations from generated data in real time.
///
///  ---
///
///  ## Implementation Strategy
///
///  OpDispenser implementations are intended to be implemented
///  for each type of distinct operation that is supported by a
///  DriverAdapter.
///  That is not to say that an OpDispenser can't be responsible for
///  producing multiple types of operations. Operations which are similar
///  in what they need and how they are constructed make sense to be implemented
///  in the same op dispenser. Those which need different construction
///  logic or fundamentally different types of field values should be implemented
///  separately. The rule of thumb is to ensure that op construction patterns
///  are easy to understand at the mapping level ([OpMapper])
///  and streamlined for fast execution at the synthesis level ([OpDispenser])
/// @param <OPTYPE>
///     The parameter type of the actual operation which will be used to hold all the details for
///         executing an
///             operation, something that implements [CycleOp]

public interface OpDispenser<OPTYPE extends CycleOp<?>> extends LongFunction<OPTYPE>, OpResultTracker {

    /// This method should do all the work of
    /// creating an operation that is executable by some other caller.
    /// The value produced by the apply method should not require
    /// additional processing if a caller wants to execute the operation
    /// multiple times, as for retries.
    /// @param cycle
    ///     The cycle number which serves as the seed for any
    ///                                      generated op fields to be bound into an operation.
    /// @return an executable operation
    OPTYPE getOp(long cycle);

    CycleFunction<Boolean> getVerifier();

    String getOpName();
}
