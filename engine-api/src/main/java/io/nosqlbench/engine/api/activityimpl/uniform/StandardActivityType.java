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

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityconfig.OpsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.NBReconfigurable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class StandardActivityType<A extends StandardActivity<?,?>> extends SimpleActivity implements ActivityType<A> {

    private final static Logger logger = LogManager.getLogger("ACTIVITY");
    private final Map<String,DriverAdapter> adapters = new HashMap<>();

    public StandardActivityType(DriverAdapter<?,?> adapter, ActivityDef activityDef) {
        super(activityDef);
        this.adapters.put(adapter.getAdapterName(),adapter);
        if (adapter instanceof ActivityDefAware) {
            ((ActivityDefAware) adapter).setActivityDef(activityDef);
        }
    }

    public StandardActivityType(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public A getActivity(ActivityDef activityDef) {
        if (activityDef.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("This driver does not support async mode yet.");
        }

        return (A) new StandardActivity(activityDef);
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        for (DriverAdapter adapter : adapters.values()) {
            if (adapter instanceof NBReconfigurable reconfigurable) {
                NBConfigModel cfgModel = reconfigurable.getReconfigModel();
                Optional<String> op_yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
                if (op_yaml_loc.isPresent()) {
                    Map<String,Object> disposable = new LinkedHashMap<>(activityDef.getParams());
                    OpsDocList workload = OpsLoader.loadPath(op_yaml_loc.get(), disposable, "activities");
                    cfgModel=cfgModel.add(workload.getConfigModel());
                }
                NBConfiguration cfg = cfgModel.apply(activityDef.getParams());
                reconfigurable.applyReconfig(cfg);
            }
        }

    }

    @Override
    public ActionDispenser getActionDispenser(A activity) {
        return new StandardActionDispenser(activity);
    }


}
