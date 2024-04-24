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

package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class StandardActivityType<A extends StandardActivity<?,?>> implements ActivityType<A> {

    private static final Logger logger = LogManager.getLogger("ACTIVITY");
    private final Map<String, DriverAdapter> adapters = new HashMap<>();
    private final NBComponent parent;
//    private final DriverAdapter<?, ?> adapter;
    private final ActivityDef activityDef;

    public StandardActivityType(final DriverAdapter<?,?> adapter, final ActivityDef activityDef, final NBComponent parent) {
        this.parent = parent;
//        this.adapter = adapter;
        this.activityDef = activityDef;
//        super(parent,activityDef
//            .deprecate("type","driver")
//            .deprecate("yaml", "workload")
//        );
        adapters.put(adapter.getAdapterName(),adapter);
        if (adapter instanceof ActivityDefAware) ((ActivityDefAware) adapter).setActivityDef(activityDef);
    }

    public StandardActivityType(final ActivityDef activityDef, final NBComponent parent) {
        this.parent = parent;
        this.activityDef = activityDef;

//        super(parent,activityDef);
    }

    @Override
    public A getActivity(final ActivityDef activityDef, final NBComponent parent) {
        if (activityDef.getParams().getOptionalString("async").isPresent())
            throw new RuntimeException("This driver does not support async mode yet.");

        return (A) new StandardActivity(parent, activityDef);
    }


    @Override
    public ActionDispenser getActionDispenser(final A activity) {
        return new StandardActionDispenser(activity);
    }

}
