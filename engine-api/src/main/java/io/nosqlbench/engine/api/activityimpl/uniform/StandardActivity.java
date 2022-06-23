/*
 * Copyright (c) 2022 nosqlbench
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

import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * This is a typed activity which is expected to become the standard
 * core of all new activity types. Extant NB drivers should also migrate
 * to this when possible.
 *
 * @param <R> A type of runnable which wraps the operations for this type of driver.
 * @param <S> The context type for the activity, AKA the 'space' for a named driver instance and its associated object graph
 */
public class StandardActivity<R extends Op, S> extends SimpleActivity {
    private final static Logger logger = LogManager.getLogger("ACTIVITY");

    private final DriverAdapter<R, S> adapter;
    private final OpSequence<OpDispenser<? extends R>> sequence;
    private final NBConfigModel yamlmodel;

    public StandardActivity(DriverAdapter<R, S> adapter, ActivityDef activityDef) {
        super(activityDef);
        this.adapter = adapter;

        if (adapter instanceof NBConfigurable configurable) {
            NBConfigModel cmodel = configurable.getConfigModel();
            Optional<String> yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
            if (yaml_loc.isPresent()) {
                Map<String,Object> disposable = new LinkedHashMap<>(activityDef.getParams());
                StmtsDocList workload = StatementsLoader.loadPath(logger, yaml_loc.get(), disposable, "activities");
                yamlmodel = workload.getConfigModel();
            }
            else {
                yamlmodel= ConfigModel.of(StandardActivity.class).asReadOnly();
            }
            NBConfigModel combinedModel = cmodel.add(yamlmodel);
            NBConfiguration configuration = combinedModel.apply(activityDef.getParams());
            configurable.applyConfig(configuration);
        }
        else {
            yamlmodel= ConfigModel.of(StandardActivity.class).asReadOnly();
        }

        try {
            OpMapper<R> opmapper = adapter.getOpMapper();
            Function<Map<String, Object>, Map<String, Object>> preprocessor = adapter.getPreprocessor();
            boolean strict = activityDef.getParams().getOptionalBoolean("strict").orElse(false);
            sequence = createOpSourceFromCommands(opmapper, adapter.getConfiguration(), List.of(preprocessor), strict);
        } catch (Exception e) {
            if (e instanceof OpConfigError) {
                throw e;
            } else {
                throw new OpConfigError("Error mapping workload template to operations: " + e.getMessage(), null, e);
            }
        }
    }

    @Override
    public void initActivity() {
        super.initActivity();
        setDefaultsFromOpSequence(sequence);
    }

    public OpSequence<OpDispenser<? extends R>> getOpSequence() {
        return sequence;
    }

    /**
     * When an adapter needs to identify an error uniquely for the purposes of
     * routing it to the correct error handler, or naming it in logs, or naming
     * metrics, override this method in your activity.
     *
     * @return A function that can reliably and safely map an instance of Throwable to a stable name.
     */
    @Override
    public final Function<Throwable, String> getErrorNameMapper() {
        return adapter.getErrorNameMapper();
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        if (adapter instanceof NBReconfigurable configurable) {
            NBConfigModel cfgModel = configurable.getReconfigModel();
            NBConfiguration cfg = cfgModel.matchConfig(activityDef.getParams());
            NBReconfigurable.applyMatching(cfg,List.of(configurable));
        }
//
//        ActivityDefObserver.apply(activityDef, adapter, sequence);
    }

//    @Override
//    public List<OpTemplate> getDefaultTemplates(StmtsDocList optionalDocs) {
//        if (adapter instanceof DefaultOpTemplateSupplier s) {
//            return s.getDefaultTemplates(optionalDocs);
//        } else {
//            return List.of();
//        }
//    }
}
