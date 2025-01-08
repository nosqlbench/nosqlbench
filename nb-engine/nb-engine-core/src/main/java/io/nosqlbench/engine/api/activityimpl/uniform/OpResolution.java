package io.nosqlbench.engine.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.engine.util.Tagged;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.Map;

/// This type represents the process of op synthesis, including all upstream sources
/// of data or configuration. This makes the stages explicit, identifies the functional
/// patterns used to create a _higher-order_ op synthesis, and allows for a fully lazy-init
/// form to be used. It also makes it possible for some op mapping logic to include any necessary
/// views of other ops, such as those used by reference to keep complex configurations DRY.
///
/// This logic is __ENTIRELY__ setup logic, acting as an implicity op compiler before selected ops
/// are ready to be used in an activity. Most of the stack constructions here will fall away to GC
/// after the final assembled structures are captured at the end of op synthesis. As such, __DO
/// NOT__
/// worry about over-presenting or being overly pedantic in this layer of code. It is more
/// important
/// to illustrate with clarity how this process works rather than to obsess over terseness or
/// optimizations here.
///
/// To ensure consistency, these methods should be guarded as synchronized. It is not gauranteed
/// that this
/// system will not be used in a multi-threaded way in the future, and this is a simple way to
/// ensure
/// atomicity during the graph construction. (Remember, this is not a critical performance path.)
///
/// Further, to avoid auto-recursion, each step of
/// resolution is guarded by a boolean which is used to detect when a particular __unresolved__ node
/// in the dependency
/// graph is resolved again. Basic graph theory says this should never happen unless some element is
/// dependent upon
/// itself through a circular reference. (in graph vernacular, a _cycle_, but that would be
/// confusing!) A
/// future improvement to this error-detection feature should be able to
/// look at the stack and extract the signature of traversal without the need to add explicit
/// traversal tracking data.
/// Even the latter may be useful if users start to build too many circular configurations.
public class OpResolution implements Tagged {

    /// starting point
    private OpTemplate template;
    private final Activity activity;

    /// adapter is resolved from the 'driver' op field, deferring to the config (activityConfig) for
    /// the default value if needed. (it usually comes from there)
    private boolean resolvingAdapter;
    private final AdapterResolver adapterF;
    private DriverAdapter<? extends CycleOp<?>, ? extends Space> adapter = null;

    /// parsed ops are object-level _normalized_ APIs around the op template and activity params
    private boolean resolvingParsedOp = false;
    private ParsedOpResolver parsedOpResolver;
    private ParsedOp parsedOp;

    /// op dispensers yield executable ops from cycle numbers
    /// NOTE: the op mapping layer is folded into this, as mappers are 1:1 with adapters
    private boolean resolvingDispenser = false;
    private final DispenserResolver dispenserResolver;
    private OpDispenser<? extends CycleOp<?>> dispenser;

    /// This can be provided to any layer which neds access to context info during resolution.
    /// It might be better as a part of a service or context view which is handed in to every layer
    private final OpResolverBank resolver;

    public OpResolution(
        Activity activity,
        AdapterResolver adapterResolver,
        OpTemplate template,
        ParsedOpResolver parsedOpResolver,
        DispenserResolver dispenserResolver,
        OpResolverBank resolver
    )
    {
        this.activity = activity;
        this.adapterF = adapterResolver;
        this.template = template;
        this.parsedOpResolver = parsedOpResolver;
        this.dispenserResolver = dispenserResolver;
        this.resolver = resolver;
    }

    /// This is the top-level product of this resolution layer. An [OpDispenser] can produce a
    /// stable and type-specific [CycleOp] for a given coordinate. It will call other more
    /// primitive layers as needed to get the components or component functions around which to
    /// build the final dispenser. These elements may be initialized by side-effect of other
    /// operations being resolved. In every case, once an element of any op resolution is
    /// resolved, it should be considered _defined_, and not resolved again.
    public synchronized <OPTYPE extends CycleOp<?>, SPACETYPE extends Space> OpDispenser<OPTYPE> resolveDispenser() {
        if (resolvingDispenser == true) {
            throw new OpConfigError("Cyclic reference while resolving dispenser for op '" +
                                    template.getName() +
                                    "'");
        }
        resolvingDispenser = true;
        if (dispenser == null) {
            this.dispenser = dispenserResolver.apply(resolveAdapter(), getParsedOp());
            //            ParsedOp pop = resolveParsedOp();
            //            DriverAdapter<OPTYPE, SPACETYPE> adapter = (DriverAdapter<OPTYPE, SPACETYPE>) resolveAdapter();
            //            // TODO verify whether or not it is necessary to ensure static mapping between adapter and mapper instance
            //            this.dispenser = adapter.getOpMapper().apply(adapter, pop, adapter.getSpaceFunc(pop));
        }
        resolvingDispenser = false;
        return (OpDispenser<OPTYPE>) dispenser;
    }

    /// Converting an op template into a ParsedOp means back-filling it with config data from the
    /// activity parameter.
    public synchronized <OPTYPE extends CycleOp<?>, SPACETYPE extends Space> ParsedOp getParsedOp() {
        if (resolvingParsedOp == true) {
            throw new OpConfigError("Cyclic reference while resolving dispenser for op '" +
                                    template.getName() +
                                    "'");
        }
        resolvingParsedOp = true;
        if (parsedOp == null) {
            this.parsedOp = parsedOpResolver.apply(activity, resolveAdapter(), template);
        }
        resolvingParsedOp = false;
        return parsedOp;
    }

    /// Each op template is interpreted by a specific [OpMapper] designated by the `driver` op
    /// field. Thus
    /// the associated driver needs to be loaded.
    private synchronized <OPTYPE extends CycleOp<?>, SPACETYPE extends Space> DriverAdapter<OPTYPE, SPACETYPE> resolveAdapter() {
        if (resolvingAdapter) {
            throw new OpConfigError("Cyclic reference while resolving adapter for op '" +
                                    template.getName() +
                                    "'");
        }
        resolvingAdapter = true;
        String
            driverName =
            template.getOptionalStringParam("driver", String.class)
                .or(() -> activity.getConfig().getOptional("driver")).orElse("stdout");
        this.adapter = adapterF.apply(activity, driverName, activity.getConfig());
        resolvingAdapter = false;
        return (DriverAdapter<OPTYPE, SPACETYPE>) adapter;
    }

    @Override public Map<String, String> getTags() {
        return template.getTags();
    }
}
