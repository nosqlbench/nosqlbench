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

import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;


/// The DriverAdapter interface is the top level API for implementing operations of any kind in
/// NoSQLBench. It defines the related APIs needed to fully realize an adapter at runtime. A
/// driver adapter can map op templates from YAML form to a fully executable form in native Java
/// code, and then execute those native operations just as an application might do directly
/// with a native driver. It can also do trivial operations, like simply calling
/// `System.out.println(...)`. What a particular driver adapter does is open-ended, but this
/// usually means wrapping a native driver when supporting specific protocols.
///
/// Every DriverAdapter has a simple name, as indicated on it's [Service] annotation. When you
/// specify an adapter by name, you are choosing both the available operations, and the rules
/// for converting a YAML op template into those operations. This is a two-step process: Mapping
/// user intentions, and generating executable operations, called op mapping and op dispensing,
/// respectively. Each adapter provides implementations for both of these phases for all the op
/// types is supports. When used together, they power _op synthesis_ -- efficient and
/// deterministic construction of runtime operations using procedural generation methods.
///
/// An overview of all the key elements of the adapter API is given here. Understanding this
/// section means you know how the core op generator logic of NoSQLBench works.
///
/// Generally speaking, a driver adapter is responsible for
///  - Implementing a type of adapter-specific [Space] to hold related state.
///    - The type of this is specified as generic parameter [SPACETYPE] on [DriverAdapter].
///    - This is the primary state holder for any thing an application would typically need in order
/// to use a
///    (specific) native driver. Space instances are analogous to application instances, although
///    they only hold the essential state needed to enable native driver usage. By default, an
///    adapter only provides a single space, but users can override this to achieve higher native
///    driver concurrency for some specialized types of testing.
///  - In [DriverAdapter#getSpaceInitializer(NBConfiguration)], defining a factory method for
///    constructing an instance of the space when needed.
///    - The [NBConfiguration] is provided to configure app or driver settings as specified by
///      the user in activity parameters.
///  - in [OpMapper#apply(NBComponent, ParsedOp, LongFunction)], recognizing the op template
///    that is documented for it and constructing a matching [OpDispenser]`<`[OPTYPE]`>`.
///    - The [NBComponent] is part of the runtime component tree, and can be used to attach
///      user-visible component and naming structure as needed. The component tree supports
///      runtime event propogation, and automatic dimensional-labeling within the NoSQLBench
///      runtime. It also provides services for creating context-anchored metrics instruments,
///      configuration linting, and so on.
///    - The [ParsedOp] is a fully normalized view of an [OpTemplate], adhering to all the rules
///      of the _Uniform_ [workload_definition]. The parsed op API provides methods to assist in
///      constructing the lambdas which are the backbone of op synthesis. Lambdas constructed in
///      this way are highly efficient in modern JVM implementations, and serve effectively as
///      defered compilation in NoSQLBench.
///    - The [LongFunction]`<`[SPACETYPE]`>` provides functional access to the space needed for an
///      operation. It is a long function, since the space instance may be specific for each
///      (long) cycle value
///  - in [OpDispenser#apply(long)]), creating an executable operation in the form of a [CycleOp].
///    - Op dispenser logic should call a previously constructed lambda, having been built either
/// in
///      the body of [OpMapper#apply(NBComponent, ParsedOp, LongFunction)] or in the constructor of
///      [OpDispenser]. In either case, the op synthesis function is realized before the op
///      dispenser is fully constructed, ensuring that op generation is streamlined.
///    - The long value provided here is the cycle value, and is the primary _coordinate_ provided
///      to the op synthesis lambda. Any properties or features of the operation should be fully
///      determined, and a matching immutable op implementation should be returned.
///  - implement [CycleOp#apply(long)] for each unique type of operation which can be dispensed.
///    - CycleOps should be immutable, since they may be retried when necessary.
///      Subsequent calls should exactly the same thing for a given op instance.
///    - the long cycle value is provided here for special cases like error handling, logging,
///      or debugging, but should not be needed for normal op execution.
///
/// ---
///
/// At a high level, the following sequence is executed for every cycle in an activity:
/// ```
///[cycle value] -> op synthesis -> [executable op] -> op execution
///```
/// or, as a functional sketch, `opExecution(opFunction(cycle))`.  This is a simplified view of the
/// detailed steps, most of which are handled automatically by the nosqlbench runtime engine:
///
/// ```
/// cycle value
/// -> op template sequencing       # memoized
/// -> op template selection        # memoized
/// -> op template parsing          # memoized
/// -> op template normalization    # memoized
/// -> op type mapping              # memoized
/// -> op dispensing
/// -> op execution
///```
///
/// Notice that some stages are optimized via a form of memoization, for efficient execution.
///
/// ---
///
/// ### Variable Naming Conventions
///
/// Within the related [DriverAdapter] APIs, the following conventions are (more often) used, and will be
/// found everywhere:
/// - `namedF` describes a namedFunction variable. Functional patterns are used everywhere in these APIs.
/// - `namedC` describes a namedComponent variable. All key elements of the nosqlbench runtime are part of a
///   component tree.
/// - `pop` describes a [ParsedOp] instance.
///
/// ---
///
/// ### Generic Parameters
///
/// When a new driver adapter is defined with the generic parameters below, it becomes easy to build out a matching
/// [DriverAdapter] with any modern IDE by using the _implement interface ..._ and similar features.
///
/// @param OPTYPE
///     The type of [CycleOp] which will be used to wrap all operations for this driver adapter.
///     This allows you to add context or features common to all operations of this type. This can
///     be a simple
///     [marker](https://en.wikipedia.org/wiki/Marker_interface_pattern) interface,
///         or it can be something more concrete that captures common logic or state across all the
///     operations used for a given adapter. It is highly advised to _NOT_ leave it as simply
///     `CycleOp<?>`, since specific op implementations offer much better performance.
/// @param SPACETYPE
///     The type of context space used by this driver to hold cached instances of clients,
///     session, or other native driver state. This is the shared state which might be needed
///     during construction operations for an adapter. No other mechanism is provided nor intended
///     for holding adapter-specific state. You must store it in this type. This includes client
///     instances, codec mappings, or anything else that a single instance of an application would
///     need to effectively use a given native driver.
///
public interface DriverAdapter<OPTYPE extends CycleOp<?>, SPACETYPE extends Space> extends NBComponent {

    /// ## Op Mapping
    ///
    /// An Op Mapper is a function which can look at a [ParsedOp] and create a matching
    /// [OpDispenser].
    /// An OpDispenser is a function that will produce a special type [OPTYPE] that this
    /// DriverAdapter implements.
    /// There may be many different ops supported by an adapter, thus there may be similarly many
    /// dispensers.
    ///
    /// Both [OpMapper] and [OpDispenser] are functions. The role of [OpMapper] is to map the op
    /// template provided
    /// by the user to an op implementation provided by the driver adapter, and then to create a
    /// suitable function for
    /// creating that type of operations, known as the [OpDispenser].
    ///
    /// These roles are split for a very good reason: Mapping what the user wants to do with an op
    /// template
    /// is resource intensive, and should be as pre-baked as possible. This phase is the _op
    /// mapping_ phase.
    /// It is essential that the mapping logic be very clear and maintainable. Performance is not as
    /// important
    /// at this phase, because all of the mapping logic is run during initialization of an
    /// activity.
    ///
    /// Conversely, _op dispensing_ (the next phase) while an activity is running should be as
    /// efficient as possible.
    /// @return a dispensing function for an [OPTYPE] op generation
    OpMapper<OPTYPE, SPACETYPE> getOpMapper();

    /// The preprocessor function allows the driver adapter to remap the fields in the op template
    /// before they are
    /// interpreted canonically. At this level, the transform is applied once to the input map (once
    /// per op
    /// template) to yield the map that is provided to [OpMapper] implementations. _This is here to
    /// make
    /// backwards compatibility possible for op templates which have changed. Avoid using it unless
    /// necessary._
    /// @return A function to pre-process the op template fields.
    default Function<Map<String, Object>, Map<String, Object>> getPreprocessor() {
        return f -> f;
    }


    /// When a driver needs to identify an error uniquely for the purposes of routing it to the
    /// correct error
    /// handler, or naming it in logs, or naming metrics, override this method in your activity.
    /// @return A function that can reliably and safely map an instance of Throwable to a stable
    /// adapter-specific name.
    default Function<Throwable, String> getErrorNameMapper() {
        return t -> t.getClass().getSimpleName();
    }

    default List<Function<Map<String, Object>, Map<String, Object>>> getOpFieldRemappers() {
        return List.of(f -> f);
    }

    /// This method allows each driver adapter to create named state which is automatically cached
    /// and re-used by
    /// name. For each (driver,space) combination in an activity, a distinct space instance will be
    /// created. In
    /// general, adapter developers will use the space type associated with an adapter to wrap
    /// native driver
    /// instances one-to-one. As such, if the space implementation is an [AutoCloseable], it will
    /// be
    /// explicitly shutdown as part of the activity shutdown.
    ///
    /// It is not necessary to implement a space for a stateless driver adapter, or one which
    /// injects all necessary
    /// state into each op instance.
    /// @return A function which can initialize a new Space, which is a place to hold object state
    ///  related to
    /// retained objects for the lifetime of a native driver.
    default LongFunction<SPACETYPE> getSpaceInitializer(NBConfiguration cfg) {
        return n -> (SPACETYPE) new Space() {
            @Override
            public String getName() {
                return "empty_space";
            }
        };
    }

    /// Provides the configuration for this driver adapter, which comes from the superset of
    /// activity parameters given for the owning activity. Presently, the driver adapter acts
    /// as a proxy to set these parameters on the space, but this will likely be updated.
    /// Instead, the configuratin will be properly attached to the space directly, and the APIs
    /// supporting it will enforce this.
    NBConfiguration getConfiguration();

    /// The standard way to provide docs for a driver adapter is to put them in one of two common
    /// places:
    /// - `resources/<adaptername>.md`
    ///   - A single markdown file which is the named top-level markdown file for this driver
    /// adapter.
    /// - `resources/docs/<adaptername>/`
    ///   - A directory containing any type of file which is to be included in docs under the
    /// adapter name, otherwise
    ///     known as the [Service#selector()]
    /// - `resources/docs/<adaptername>.md`
    ///   - An alternate location for the main doc file for an adapter, assuming you are using the
    /// docs/ path.
    ///
    /// _A build will fail if any driver adapter implementation is missing at least one self-named
    /// markdown doc file._
    /// @return A [DocsBinder] which describes docs to include for a given adapter.
    default DocsBinder getBundledDocs() {
        Docs docs = new Docs().namespace("drivers");
        String dev_docspath = "adapter-" + this.getAdapterName() + "/src/main/resources/docs/" + this.getAdapterName();
        String cp_docspath = "docs/" + this.getAdapterName();
        Optional<Content<?>> bundled_docs = NBIO.local().pathname(
            dev_docspath, cp_docspath).first();
        bundled_docs.map(Content::asPath).ifPresent(docs::addContentsOf);
        String markdown = this.getAdapterName() +".md";
        System.out.println("Markdown: " +   markdown);
        Optional<Content<?>> maindoc = NBIO.local().pathname(
            "/src/main/resources/" + markdown, markdown
        ).first();
        if ( !maindoc.isPresent() ) {
            System.out.println("Could not find main document for " + markdown);
        }
        maindoc.map(Content::asPath).ifPresent(docs::addPath);
        return docs.asDocsBinder();
    }

    /// Provide the simple name for this [DriverAdapter] implementation, derived from the
    /// required [Service] annotation.
    default String getAdapterName() {
        Service svc = this.getClass().getAnnotation(Service.class);
        if (svc == null) {
            throw new RuntimeException(
                "The Service annotation for adapter of type " + this.getClass().getCanonicalName() + " is missing.");
        }
        return svc.selector();
    }

    /// Indicate the level of testing and muturity for the current adapter. This is not actively
    /// used
    /// and may be removed.
    /// @deprecated
    default Maturity getAdapterMaturity() {
        return this.getClass().getAnnotation(Service.class).maturity();
    }

    /// The function returned by [#getSpaceFunc(ParsedOp)] provides access to a cache of all
    /// stateful objects needed
    /// within a single instance of a DriverAdapter. These are generally things needed by
    /// operations, or things
    /// needed during the construction of operations. Typically, a space is where you store a native
    /// driver instance
    /// which is expected to be created/initialized once and reused within an application.
    /// Generally, users can
    /// think of __space__ as __driver instance__, or __client instance__, although there are driver
    /// adapters that
    /// do things other than wrap native drivers and clients.
    ///
    /// The value of the op field `space` is used to customize the instancing behavior of spaces. If
    /// none is provided
    /// by the user, then only a singular space will be created for a given adapter in an activity.
    /// This is normal,
    /// and what most users will expect to do. However, customizing the space selector can be a
    /// powerful way to test
    /// any system with high logical concurrency. For example, each instance of a native driver will
    /// typically
    /// maintain its own thread or connection pools, cached resources and so on.  ( Unless the
    /// developers  of
    /// said native driver are breaking encapsulation by using global singletons in the runtime,
    /// which is highly
    /// frowned upon.) The spaces feature allows any nosqlbench workload to be easily converted into
    /// an unreasonably parallel
    /// client topology test with a single change. It works the same way for any adapter or protocol
    /// supported by
    /// nosqlbench.
    ///
    /// The value of the op field `space` should be a [Number] type. [Number#intValue()] is used to
    /// determine which space instance is, if needed, initialized first, and then returned. If users provide a
    /// non-[Number] type,
    /// then an enumerating layer is added inline to convert any such value to an integer, which is
    /// less optimal.
    ///
    /// During op mapping or dispensing, you may need access to state held by a driver-specific
    /// implementation of
    /// [SPACETYPE]. In the initialization phase, you only have access to the space function
    /// itself.
    /// This is important to maintain a boundary betwen the explicitly stateless and stateful parts
    /// of the
    /// runtime. To use the space, incorporate the space function into the lambdas which produce the
    /// operation to be
    /// executed. This is typically done in the construtor of the related [OpDispenser].
    /// @return A cache of named objects
    public LongFunction<SPACETYPE> getSpaceFunc(ParsedOp pop);

}
