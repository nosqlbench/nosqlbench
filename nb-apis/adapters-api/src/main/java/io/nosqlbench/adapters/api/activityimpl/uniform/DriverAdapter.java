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
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.nb.api.docsapi.Docs;
import io.nosqlbench.nb.api.docsapi.DocsBinder;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.annotations.Maturity;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * <P>The DriverAdapter interface is the top level API for implementing
 * operations in NoSQLBench. It defines the related APIs needed to fully realized an adapter
 * at runtime. A driver adapter can map op templates from YAML form to a fully executable
 * form in native Java code, just as an application might do with a native driver. It can also
 * do trivial operations, like simply calling {@code System.out.println(...)}. When you specify an adapter by name,
 * you are choosing both the available operations, and the rules for converting a YAML op template into those
 * operations. This is a two-step process: The adapter provides mapping logic for converting high-level templates from
 * users into op types, and separate dispensing logic which can efficiently create these ops at runtime. When used
 * together, they power <EM>op synthesis</EM> -- efficient and deterministic construction of runtime operations using
 * procedural generation methods.
 * </p>
 *
 * <p>
 * Generally speaking, a driver adapter is responsible for
 * <UL>
 * <LI>Defining a class type for holding related state known as a {@link Space}.
 * <UL><LI>The type of this is specified as
 * generic parameter {@link SPACETYPE}.</LI></UL></LI>
 * <LI>Defining a factory method for constructing an instance of the space.</LI>
 * <LI>Recognizing the op templates that are documented for it via an {@link OpMapper}
 * and assigning them to an op implementation.
 * <UL><LI>The base type of these ops is specified as generic
 * parameter {@link OPTYPE}.</LI></UL>
 * </LI>
 * <LI>Constructing dispensers for each matching op implementation with a matching {@link OpDispenser}</LI>
 * implementation.
 * </UL>
 * <P>At runtime, the chain of these together ({@code cycle -> op mapping -> op dispensing -> op}) is cached
 * as a look-up table of op dispensers. This results in the simpler logical form {@code cycle -> op synthesis ->
 * operation}.
 * </P>
 *
 * <H3>Variable Naming Conventions</H3>
 * <p>
 * Within the related {@link DriverAdapter} APIs, the following conventions are (more often) used, and will be found
 * everywhere:
 * <UL>
 * <LI>{@code namedF} describes a namedFunction variable. Functional patterns are used everywhere in these APIs
 * .</LI>
 * <LI>{@code namedC} describes a namedComponent variable. All key elements of the nosqlbench runtime are
 * part of a component tree.</LI>
 * <LI>{@code pop} describes a {@link ParsedOp} instance.</LI>
 * </UL>
 * </P>
 * <H3>Generic Parameters</H3>
 * <p>
 * When a new driver adapter is defined with the generic parameters below, it becomes easy to build out a matching
 * DriverAdapter with any modern IDE.</P>
 *
 * @param <OPTYPE>
 *     The type of {@link CycleOp} which will be used to wrap all operations for this driver adapter. This allows you
 *     to add context or features common to all operations of this type. This can be a simple <a
 *     href="https://en.wikipedia.org/wiki/Marker_interface_pattern">Marker</a> interface, or it can be something more
 *     concrete that captures common logic or state across all the operations used for a given adapter. It is highly
 *     advised to <EM>NOT</EM> leave it as simply {@code CycleOp<?>}, since specific op implementations offer much
 *     better performance.
 * @param <SPACETYPE>
 *     The type of context space used by this driver to hold cached instances of clients, session, or other native
 *     driver state. This is the shared state which might be needed during construction operations for an adapter.
 *     <EM>No other mechanism is provided nor intended for holding adapter-specific state. You must store it in
 *     this type. This includes client instances, codec mappings, or anything else that a single instance of an
 *     application would need to effectively use a given native driver.</EM>
 */
public interface DriverAdapter<OPTYPE extends CycleOp<?>, SPACETYPE extends Space> extends NBComponent {

    /**
     * <p>
     * <H2>Op Mapping</H2>
     * An Op Mapper is a function which can look at a {@link ParsedOp} and create a matching {@link OpDispenser}.
     * An OpDispenser is a function that will produce a special type {@link OPTYPE} that this DriverAdapter implements
     * as its op implementation. There may be many different ops supported by an adapter, thus there may be similarly
     * many dispensers.</p>
     *
     * <p>
     * Both {@link OpMapper} and {@link OpDispenser} are functions. The role of {@link OpMapper} is to
     * map the op template provided by the user to an op implementation provided by the driver adapter,
     * and then to create a factor function for it (the {@link OpDispenser}).</p>
     *
     * <p>These roles are split for a very good reason: Mapping what the user wants to do with an op template
     * is resource intenstive, and should be as pre-baked as possible. This phase is the <EM>op mapping</EM> phase.
     * It is essential that the mapping logic be very clear and maintainable. Performance is not as important
     * at this phase, because all of the mapping logic is run during initialization of an activity.
     * </p>
     * <p>
     * Conversely, <EM>op dispensing</EM> (the next phase) while an activity is running should be as efficient as
     * possible.
     * </p>
     *
     * @return a dispensing function for {@link OPTYPE} op generation
     */
    OpMapper<OPTYPE, SPACETYPE> getOpMapper();

    /**
     * The preprocessor function allows the driver adapter to remap
     * the fields in the op template before they are interpreted canonically.
     * At this level, the transform is applied once to the input map
     * (once per op template) to yield the map that is provided to
     * {@link OpMapper} implementations. <EM>This is here to make backwards compatibility
     * possible for op templates which have changed. Avoid using it unless necessary.</EM>
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
     * @return A function that can reliably and safely map an instance of Throwable to a stable adapter-specific name.
     */
    default Function<Throwable, String> getErrorNameMapper() {
        return t -> t.getClass().getSimpleName();
    }

    default List<Function<Map<String, Object>, Map<String, Object>>> getOpFieldRemappers() {
        return List.of(f -> f);
    }

    /**
     * <P>This method allows each driver adapter to create named state which is automatically
     * cached and re-used by name. For each (driver,space) combination in an activity,
     * a distinct space instance will be created. In general, adapter developers will
     * use the space type associated with an adapter to wrap native driver instances
     * one-to-one. As such, if the space implementation is a {@link AutoCloseable},
     * it will be explicitly shutdown as part of the activity shutdown.</P>
     *
     * <p>It is not necessary to implement a space for a stateless driver adapter, or one
     * which puts all state into each op instance.</p>
     *
     * @return A function which can initialize a new Space, which is a place to hold
     *     object state related to retained objects for the lifetime of a native driver.
     */
    default LongFunction<SPACETYPE> getSpaceInitializer(NBConfiguration cfg) {
        return n -> (SPACETYPE) new Space() {
            @Override
            public String getName() {
                return "empty_space";
            }
        };
    }

    NBConfiguration getConfiguration();

    /**
     * The standard way to provide docs for a driver adapter is to put them in one of two common places:
     * <ul>
     *     <li>&lt;resources&gt;/&lt;adaptername&gt;.md - A single markdown file which is the named top-level
     *     markdown file for this driver adapter.</li>
     *     <li>&lt;resources&gt;/docs/&lt;adaptername&gt;/ - A directory containing any type of file which
     *     is to be included in docs under the adapter name, otherwise known as the {@link Service#selector()}</li>
     *     <li>&lt;resources&gt;/docs/&lt;adaptername&gt;.md</li>
     * </ul>
     *
     * <P><EM>A build will fail if any driver adapter implementation is missing at least one self-named
     * markdown doc file.</EM></P>
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
        Service svc = this.getClass().getAnnotation(Service.class);
        if (svc == null) {
            throw new RuntimeException("The Service annotation for adapter of type " + this.getClass().getCanonicalName() + " is missing.");
        }
        return svc.selector();
    }

    default Maturity getAdapterMaturity() {
        return this.getClass().getAnnotation(Service.class).maturity();
    }

    /**
     * <p>The cache of all objects needed within a single instance
     * of a DriverAdapter which are not operations. These are generally
     * things needed by operations, or things needed during the
     * construction of operations.</p>
     *
     * <p>During Adapter Initialization, Op Mapping, Op Synthesis, or Op Execution,
     * you may need access to the objects in (the or a) space cache. You can build the
     * type of context needed and then provide this function to provide new instances
     * when needed.</p>
     *
     * <p>The function returned by this method is specialized to the space mapping
     * logic in the op template. Specifically, it uses whatever binding is set on a given
     * op template for the <em>space</em> op field. If none are provided, then this
     * becomes a short-circuit for the default '0'. If a non-numeric binding is provided,
     * then an interstitial mapping is added which converts the {@link Object#toString()}
     * value to ordinals using a hash map. This is less optimal by far than using
     * any binding that produces a {@link Number}.</p>
     *
     * @return A cache of named objects
     */
    public LongFunction<SPACETYPE> getSpaceFunc(ParsedOp pop);

}
