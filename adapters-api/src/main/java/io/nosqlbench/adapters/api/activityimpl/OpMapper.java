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

import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.Function;

/**
 * <p>
 * <h2>Synopsis</h2>
 * An OpMapper is responsible for converting parsed op templates
 * into dispensers of operations based on the intention of the user.
 *
 * Op Templates as expressed as a set of field values, some literal, and
 * some dynamic, to be generated based on a specific cycle value.
 * </p>
 *
 * <p>
 * <h2>Concepts</h2>
 * The OpMapper is basically a function that returns another function. The responsibility
 * for creating executable operations is shared between the {@link OpMapper} and the
 * {@link OpDispenser}. The logic needed to determine the type of an operation intended
 * by the user (mapping) is different from the logic you use to construct that specific
 * type of operation once you know the intent (dispensing). If you look at a example
 * of doing these together in code, there is always a point at which you know what is
 * needed to construct an operation. If you draw a line at this point, it represents
 * the separation of responsibilities between op mappers and op dispensers.
 * </p>
 *
 * <p>This separation of responsibilities serves as both a conceptual clarification as
 * well as a way to optimize runtime behavior. In the NoSQLBench model, all of the first step
 * (mapping, the responsibility of this class) occurs at initialization time of an activity.
 * This means that mapping logic can be as clear, readable, type-safe and obvious as
 * possible without any negative effect on the later phase. In fact, clarity and obviousness
 * at this level serves to keep implementations of the next phase much more straight-forward
 * and streamlined, since all that is left to do is assemble the known elements together
 * into an executable operation.</p>
 *
 * </hr>
 * <h2>Implementation Strategy</h2>
 * <p>
 * A view of an op template is provided in the {@link ParsedOp} API. This allows
 * you to examine the fields provided by users. It also lets you see which
 * of these fields are defined as dynamic and which are simply static values.
 * When multiple types of operations are supported for a driver adapter, you must decide
 * on a distinct signature
 *
 * </p>
 *
 * <p>
 * <h2>Documentation Requirements</h2>
 * The logic which is implemented in the OpMapper must follow closely with the op construction
 * rules provided to the user. Conversely, the driver maintainer should take care to provide
 * rules of construction and examples in the documentation.
 * Each {@link DriverAdapter} has a unique
 * name. The documentation
 * for each of these should be kept in the bundled resources in a top-level markdown file that
 * matches the driver name.
 * </p>
 *
 * @param <OPTYPE> The parameter type of the actual operation which will be used
 *            to hold all the details for executing an operation,
 *            generally something that implements {@link Runnable}.
 */
public interface OpMapper<OPTYPE extends Op> extends Function<ParsedOp, OpDispenser<? extends OPTYPE>> {

    /**
     * Interrogate the parsed command, and provide a new
     *
     * @param op The {@link ParsedOp} which is the parsed version of the user-provided op template.
     *            This contains all the fields provided by the user, as well as explicit knowledge of
     *            which ones are static and dynamic.
     * @return An OpDispenser which can be used to synthesize real operations.
     */
    @Override
    OpDispenser<? extends OPTYPE> apply(ParsedOp op);
}
