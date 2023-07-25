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
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.NBReconfigurable;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class StandardActivityType<A extends StandardActivity<?,?>> extends SimpleActivity implements ActivityType<A> {

    private static final Logger logger = LogManager.getLogger("ACTIVITY");
    private final Map<String, DriverAdapter> adapters = new HashMap<>();

    public StandardActivityType(final DriverAdapter<?,?> adapter, final ActivityDef activityDef, final NBLabeledElement parentLabels) {
        super(activityDef
            .deprecate("type","driver")
            .deprecate("yaml", "workload"),
            parentLabels
        );
        adapters.put(adapter.getAdapterName(),adapter);
        if (adapter instanceof ActivityDefAware) ((ActivityDefAware) adapter).setActivityDef(activityDef);
    }

    public StandardActivityType(final ActivityDef activityDef, final NBLabeledElement parentLabels) {
        super(activityDef, parentLabels);
    }

    @Override
    public A getActivity(final ActivityDef activityDef, final NBLabeledElement parentLabels) {
        if (activityDef.getParams().getOptionalString("async").isPresent())
            throw new RuntimeException("This driver does not support async mode yet.");

        return (A) new StandardActivity(activityDef, parentLabels);
    }

    @Override
    public synchronized void onActivityDefUpdate(final ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        for (final DriverAdapter adapter : this.adapters.values())
            if (adapter instanceof NBReconfigurable reconfigurable) {
                NBConfigModel cfgModel = reconfigurable.getReconfigModel();
                final Optional<String> op_yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
                if (op_yaml_loc.isPresent()) {
                    final Map<String, Object> disposable = new LinkedHashMap<>(activityDef.getParams());
                    final OpsDocList workload = OpsLoader.loadPath(op_yaml_loc.get(), disposable, "activities");
                    cfgModel = cfgModel.add(workload.getConfigModel());
                }
                final NBConfiguration cfg = cfgModel.apply(activityDef.getParams());
                reconfigurable.applyReconfig(cfg);
            }

    }

    @Override
    public ActionDispenser getActionDispenser(final A activity) {
        return new StandardActionDispenser(activity);
    }


}
