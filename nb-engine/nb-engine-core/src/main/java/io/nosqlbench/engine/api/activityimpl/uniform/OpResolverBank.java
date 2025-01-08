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
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplates;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.tagging.TagFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OpResolverBank {
    private final OpTemplates optpl;
    private final List<OpResolution> resolvers = new ArrayList<>();
    private final TagFilter tagFilter;

    public OpResolverBank(
        Activity activity,
        AdapterResolver adapterF,
        OpTemplates reference,
        String tagFilter,
        DispenserResolver dispF,
        ParsedOpResolver popF,
        NBConfiguration config
    )
    {
        this.optpl = reference;
        this.tagFilter = new TagFilter(tagFilter);
        OpTemplates activeOpTemplates = reference.matching(tagFilter, false);
        if (reference.size() > 0 && activeOpTemplates.size() == 0) {
            String message =
                "There were no active op templates with tag filter '" + tagFilter + "', since all "
                + reference.size() + " were filtered out. Examine the session log for details";
        }

        for (OpTemplate opTemplate : activeOpTemplates) {
            OpResolution opres =
                new OpResolution(activity, adapterF, opTemplate, popF, dispF, this);
            resolvers.add(opres);
        }
    }

    public List<OpDispenser<? extends CycleOp<?>>> resolveDispensers() {
        List<OpDispenser<? extends CycleOp<?>>> dispensers = new ArrayList<>(resolvers.size());
        for (OpResolution resolver : resolvers) {
            OpDispenser<? extends CycleOp<?>> dispenser = resolver.resolveDispenser();
            dispensers.add(dispenser);
        }
        return dispensers;
    }

}
