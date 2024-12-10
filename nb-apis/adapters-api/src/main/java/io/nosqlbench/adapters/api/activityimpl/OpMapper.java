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
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;

import java.util.function.LongFunction;

/**
 * <p>
 * <h2>Synopsis</h2>
 * An OpMapper is responsible for converting parsed op templates
 * into dispensers of operations based on the intention of the user.
 * <p>
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
 * @param <OPTYPE>
 *     The parameter type of the actual operation which will be used
 *     to hold all the details for executing an operation,
 *     generally something that implements {@link Runnable}.
 */
public interface OpMapper<OPTYPE extends CycleOp<?>, SPACETYPE extends Space>
//    extends BiFunction<ParsedOp, LongFunction<SPACETYPE>, OpDispenser<? extends OPTYPE>>
{

    /**
     * This method is responsible for interrogating the fields in the provided {@link ParsedOp} template object,
     * determining what adapter-specific operation it maps to, and creating the associated {@link OpDispenser} for
     * that type.
     *
     * <H2>Implementation Notes</H2>
     *
     * <P>It is important to be familiar with the structure of the {@link ParsedOp}, since this is the runtime model
     * API for an op template. It provides everything you need to turn various op fields into proper lambdas, which
     * can then be composed together to make higher-order lambdas. The returned {@link OpDispenser} is essentially
     * a singular {@link LongFunction} which captures all of the just-in-time construction patterns needed within.</P>
     *
     * <H3>Op Mapping</H3>
     * <p>
     * Generally speaking, implementations of this method should interrogate the op fields in the ParsedOp to determine
     * the specific op that matches the user's intentions. It is <EM>Highly</EM> reccommended that each of the valid
     * op types is presented as an example in the associated adapter documentation. (Each adapter must have a
     * self-named markdown help file in it's source tree.) Good op mappers are based on specific examples which are
     * documented, as this is the only way a user knows what op types are available.
     * </p>
     *
     * <p>
     * What determines the type of op can be based on something explicit, like the value of a {@code type} field, or it
     * can be based on whether
     * certain fields are present or not. Advanced implementations might take into account which fields are provided as
     * static values and which are specified as (dynamic) bindings. The op mapping phase is meant to qualify and
     * pre-check that the fields provided are valid and specific for a given type of operation.
     * </p>
     *
     * <p>All of the effective logic for op mapping must be contained within the
     * {@link #apply(NBComponent, ParsedOp, LongFunction)} method. This includes what happens within the constructor of
     * any {@link OpDispenser}. What happens within {@link OpDispenser} implementations (the second phase), however,
     * should do as little qualification of field values as possible, focusing simply on constructing the type of
     * operation for which they are designed. This suggest the following conventions:
     * <UL>
     * <LI>Type-mapping logic (determine which op type) is done in the main body of
     * {@link #apply(NBComponent, ParsedOp, LongFunction)}, and nothing else. Once the desired op dispenser (based on
     * the intended op type) is determined, it is immediately constructed and returned.
     * </LI>
     * <LI>
     * Lambda-construction logic is contained in the constructor of the returned op dispenser. This pre-bakes
     * as much of the op construction behavior as possible, building only a single lambda to do the heavy lifting
     * later.
     * </LI>
     * <LI>When {@link OpDispenser#apply(long)}</LI> is called with a cycle value, it only needs to call the lambda to
     * return a fully-formed op, ready to be executed via its {@link CycleOp#apply(long)} method.
     * </UL>
     * </p>
     *
     * @param adapterC
     *     The adapter component. This is passed as an {@link NBComponent} because it is not valid to rely
     *     on the driver adapter instance directly for anything. All logic for an op should be captured
     *     in its mapper and dispenser, and all (other) state for it should be captured within the space.
     *     However, the mapper exists within the nosqlbench runtime as part of the component tree, so it
     *     is included for that reason alone. (You'll need it for super construtors in some cases)
     * @param pop
     *     The {@link ParsedOp} which is the parsed version of the user-provided op template. This contains all the
     *     fields provided by the user, as well as explicit knowledge of which ones are static and dynamic. It provides
     *     convenient lambda-construction methods to streamline the effort of creating the top-level op lambda.
     * @param spaceF
     *     This is the pre-baked lambda needed to access the specific {@link SPACETYPE} for a given cycle, if or when it
     *     is needed. Not all op types need this, since they may have all the state needed fully captured within the
     *     native type. For those that do, ensure that you are accessing the value through this function lazily and
     *     only within the stack. Using this function to do anything but build more lambdas is probably a
     *     programming error.
     * @return An OpDispenser which can be used to synthesize directly executable operations.
     */

    OpDispenser<? extends OPTYPE> apply(
        NBComponent adapterC,
        ParsedOp pop,
        LongFunction<SPACETYPE> spaceF
    );
}
