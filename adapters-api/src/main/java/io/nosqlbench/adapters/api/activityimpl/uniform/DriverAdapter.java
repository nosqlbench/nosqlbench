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

package io.nosqlbench.adapters.api.activityimpl.uniform;

import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.api.docsapi.Docs;
import io.nosqlbench.api.docsapi.DocsBinder;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * <P>The DriverAdapter interface is expected to be the replacement
 * for ActivityTypes. This interface takes a simpler
 * approach. Specifically, all of the core logic which was being pasted into each
 * driver type is centralized, and only the necessary interfaces
 * needed for construction new operations and shared context are exposed.
 * This means all drivers can now benefit from cross-cutting enhancements
 * in the core implementation.
 * </P>
 *
 * @param <OPTYPE> The type of Runnable operation which will be used to wrap
 *            all operations for this driver adapter. This allows you to
 *            add context or features common to all operations of this
 *            type.
 * @param <SPACETYPE> The type of context space used by this driver to hold
 *            cached instances of clients, session, or other native driver
 *            esoterica. This is the shared state which might be needed
 *            during construction of R type operations, or even for individual
 *            operations.
 */
public interface DriverAdapter<OPTYPE extends Op, SPACETYPE> {

    /**
     * <p>
     * <H2>Op Mapping</H2>
     * An Op Mapper is a function which can look at the parsed
     * fields in a {@link ParsedOp} and create an OpDispenser.
     * An OpDispenser is a function that will produce a special
     * type {@link OPTYPE} that this DriverAdapter implements as its
     * op implementation.</p>
     *
     * <p>
     * The function that is returned is responsible for creating another function.
     * This might seem counter-intuitive but it is very intentional because
     * of these design constraints:
     * <UL>
     * <LI>Mapping op semantics to a type of operation must be very clear
     * and flexible. Performance is not important at this layer because this is all done
     * during initialization time for an activity.</LI>
     * <LI>Synthesizing executable operations from a known type of operational template
     * must be done very efficiently. This part is done during activity execution, so
     * having the details of how you are going to create an op for execution already
     * sorted out is important.</LI>
     * </UL>
     *
     * To clarify the distinction between these two phases, the first is canonically
     * called <em>op mapping</em> in the documentation. The second is called
     * <em>op synthesis</em>.
     * </p>
     *
     * <p>
     * <H2>A note on implementation strategy:</H2>
     * Generally speaking, implementations of this method should interrogate the op fields
     * in the ParsedOp and return an OpDispenser that matches the user's intentions.
     * This can be based on something explicit, like the  value of a {@code type} field,
     * or it can be based on whether certain fields are present or not. Advanced implementations
     * might take into account which fields are provided as static values and which are
     * specified as bindings. In any case, the op mapping phase is meant to qualify and
     * pre-check that the fields provided are valid and specific for a given type of operation.
     * What happens within {@link OpDispenser} implementations (the second phase), however, should do
     * as little qualification of field values as possible, focusing simply on constructing
     * the type of operation for which they are designed.
     * </p>
     *
     * @return a synthesizer function for {@link OPTYPE} op generation
     */
    OpMapper<OPTYPE> getOpMapper();

    /**
     * The preprocessor function allows the driver adapter to remap
     * the fields in the op template before they are interpreted canonically.
     * At this level, the transform is applied once to the input map
     * (once per op template) to yield the map that is provided to
     * {@link OpMapper} implementations.
     *
     * @return A function to pre-process the op template fields.
     */
    default Function<Map<String, Object>, Map<String, Object>> getPreprocessor() {
        return f -> f;
    }

    /**
     * When a driver needs to identify an error uniquely for the purposes of
     * routing it to the correct error handler, or naming it in logs, or naming
     * metrics, override this method in your activity.
     *
     * @return A function that can reliably and safely map an instance of Throwable to a stable name.
     */
    default Function<Throwable, String> getErrorNameMapper() {
        return t -> t.getClass().getSimpleName();
    }

    default List<Function<Map<String,Object>,Map<String,Object>>> getOpFieldRemappers() {
        return List.of(f -> f);
    }

    /**
     * The cache of all objects needed within a single instance
     * of a DriverAdapter which are not operations. These are generally
     * things needed by operations, or things needed during the
     * construction of operations.
     *
     * See {@link DriverSpaceCache} for details on when and how to use this function.
     *
     * <p>During Adapter Initialization, Op Mapping, Op Synthesis, or Op Execution,
     * you may need access to the objects in (the or a) space cache. You can build the
     * type of context needed and then provide this function to provide new instances
     * when needed.</p>
     *
     * @return A cache of named objects
     */
    DriverSpaceCache<? extends SPACETYPE> getSpaceCache();

    /**
     * This method allows each driver adapter to create named state which is automatically
     * cached and re-used by name. For each (driver,space) combination in an activity,
     * a distinct space instance will be created. In general, adapter developers will
     * use the space type associated with an adapter to wrap native driver instances
     * one-to-one. As such, if the space implementation is a {@link AutoCloseable},
     * it will be explicitly shutdown as part of the activity shutdown.
     *
     * @return A function which can initialize a new Space, which is a place to hold
     * object state related to retained objects for the lifetime of a native driver.
     */
    default Function<String, ? extends SPACETYPE> getSpaceInitializer(NBConfiguration cfg) {
        return n -> null;
    }

    NBConfiguration getConfiguration();

    /**
     * The standard way to provide docs for a driver adapter is to put them in one of two common places:
     * <ul>
     *     <li>&lt;resources&gt;/&lt;adaptername&gt;.md - A single markdown file which is the named top-level
     *     markdown file for this driver adapter.</li>
     *     <li>&lt;resources&gt;/docs/&lt;adaptername&gt;/ - A directory containing any type of file which
     *     is to be included in docs under the adapter name, otherwise known as the {@link Service#selector()}</li>
     * </ul>
     * path &lt;resources&gt;/docs/&lt;adaptername&gt;. Specifically, the file
     *
     * @return A {@link DocsBinder} which describes docs to include for a given adapter.
     */
    default DocsBinder getBundledDocs() {
        Docs docs = new Docs().namespace("drivers");

        String dev_docspath = "adapter-" + this.getAdapterName() + "/src/main/resources/docs/" + this.getAdapterName();
        String cp_docspath = "docs/" + this.getAdapterName();
        Optional<Content<?>> bundled_docs = NBIO.local().pathname(dev_docspath, cp_docspath).first();
        bundled_docs.map(Content::asPath).ifPresent(docs::addContentsOf);

        Optional<Content<?>> maindoc = NBIO.local().pathname("/src/main/resources/" + this.getAdapterName() + ".md", this.getAdapterName() + ".md").first();

        maindoc.map(Content::asPath).ifPresent(docs::addPath);

        return docs.asDocsBinder();
    }

    default String getAdapterName() {
        return this.getClass().getAnnotation(Service.class).selector();
    }

    default Maturity getAdapterMaturity() {
        return this.getClass().getAnnotation(Service.class).maturity();
    }

    LongFunction<SPACETYPE> getSpaceFunc(ParsedOp pop);
}
