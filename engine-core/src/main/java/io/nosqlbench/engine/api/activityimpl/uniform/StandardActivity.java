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

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.lifecycle.Shutdownable;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.events.NBEvent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.components.events.SetThreads;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.StrideRateSpec;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.nb.annotations.ServiceSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a typed activity which is expected to become the standard
 * core of all new activity types. Extant NB drivers should also migrate
 * to this when possible.
 *
 * @param <R> A type of runnable which wraps the operations for this type of driver.
 * @param <S> The context type for the activity, AKA the 'space' for a named driver instance and its associated object graph
 */
public class StandardActivity<R extends Op, S> extends SimpleActivity implements SyntheticOpTemplateProvider, ActivityDefObserver {
    private static final Logger logger = LogManager.getLogger("ACTIVITY");
    private final OpSequence<OpDispenser<? extends Op>> sequence;
    private final ConcurrentHashMap<String, DriverAdapter<?,?>> adapters = new ConcurrentHashMap<>();

    public StandardActivity(NBComponent parent, ActivityDef activityDef) {
        super(parent,activityDef);
        OpsDocList workload;

        Optional<String> yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
        NBConfigModel yamlmodel;
        if (yaml_loc.isPresent()) {
            Map<String, Object> disposable = new LinkedHashMap<>(activityDef.getParams());
            workload = OpsLoader.loadPath(yaml_loc.get(), disposable, "activities");
            yamlmodel = workload.getConfigModel();
        } else {
            yamlmodel = ConfigModel.of(StandardActivity.class).asReadOnly();
        }

        Optional<String> defaultDriverName = activityDef.getParams().getOptionalString("driver");
        Optional<DriverAdapter<?,?>> defaultAdapter =  defaultDriverName
            .flatMap(name ->  ServiceSelector.of(name,ServiceLoader.load(DriverAdapterLoader.class)).get())
            .map(l -> l.load(this,NBLabels.forKV()));

        if (defaultDriverName.isPresent() && defaultAdapter.isEmpty()) {
            throw new BasicError("Unable to load default driver adapter '" + defaultDriverName.get() + '\'');
        }

        // HERE, op templates are loaded before drivers are loaded
        List<OpTemplate> opTemplates = loadOpTemplates(defaultAdapter.orElse(null));


        List<ParsedOp> pops = new ArrayList<>();
        List<DriverAdapter<?,?>> adapterlist = new ArrayList<>();
        NBConfigModel supersetConfig = ConfigModel.of(StandardActivity.class).add(yamlmodel);

        Optional<String> defaultDriverOption = defaultDriverName;
        ConcurrentHashMap<String, OpMapper<? extends Op>> mappers = new ConcurrentHashMap<>();
        for (OpTemplate ot : opTemplates) {
//            ParsedOp incompleteOpDef = new ParsedOp(ot, NBConfiguration.empty(), List.of(), this);
            String driverName = ot.getOptionalStringParam("driver", String.class)
                .or(() -> ot.getOptionalStringParam("type", String.class))
                .or(() -> defaultDriverOption)
                .orElseThrow(() -> new OpConfigError("Unable to identify driver name for op template:\n" + ot));

//            String driverName = ot.getOptionalStringParam("driver")
//                .or(() -> activityDef.getParams().getOptionalString("driver"))
//                .orElseThrow(() -> new OpConfigError("Unable to identify driver name for op template:\n" + ot));



            // HERE
            if (!adapters.containsKey(driverName)) {

                DriverAdapter<?,?> adapter =  Optional.of(driverName)
                    .flatMap(
                        name ->  ServiceSelector.of(
                            name,
                            ServiceLoader.load(DriverAdapterLoader.class)
                        )
                            .get())
                    .map(
                        l -> l.load(
                            this,
                            NBLabels.forKV()
                        )
                    )
                    .orElseThrow(() -> new OpConfigError("driver adapter not present for name '" + driverName + "'"));

                NBConfigModel combinedModel = yamlmodel;
                NBConfiguration combinedConfig = combinedModel.matchConfig(activityDef.getParams());

                if (adapter instanceof NBConfigurable configurable) {
                    NBConfigModel adapterModel = configurable.getConfigModel();
                    supersetConfig.add(adapterModel);

                    combinedModel = adapterModel.add(yamlmodel);
                    combinedConfig = combinedModel.matchConfig(activityDef.getParams());
                    configurable.applyConfig(combinedConfig);
                }
                adapters.put(driverName, adapter);
                mappers.put(driverName, adapter.getOpMapper());
            }

            supersetConfig.assertValidConfig(activityDef.getParams().getStringStringMap());

            DriverAdapter<?,?> adapter = adapters.get(driverName);
            adapterlist.add(adapter);
            ParsedOp pop = new ParsedOp(ot, adapter.getConfiguration(), List.of(adapter.getPreprocessor()), this);
            Optional<String> discard = pop.takeOptionalStaticValue("driver", String.class);
            pops.add(pop);
        }

        if (defaultDriverOption.isPresent()) {
            long matchingDefault = mappers.keySet().stream().filter(n -> n.equals(defaultDriverOption.get())).count();
            if (0 == matchingDefault) {
                logger.warn("All op templates used a different driver than the default '{}'", defaultDriverOption.get());
            }
        }

        try {
            sequence = createOpSourceFromParsedOps(adapterlist, pops);
        } catch (Exception e) {
            if (e instanceof OpConfigError) {
                throw e;
            }
            throw new OpConfigError("Error mapping workload template to operations: " + e.getMessage(), null, e);
        }

       create().gauge(
            "ops_pending",
           () -> this.getProgressMeter().getSummary().pending(),
           MetricCategory.Core,
           "The current number of operations which have not been dispatched for processing yet."
       );
       create().gauge(
            "ops_active",
           () -> this.getProgressMeter().getSummary().current(),
           MetricCategory.Core,
           "The current number of operations which have been dispatched for processing, but which have not yet completed."
       );
       create().gauge(
            "ops_complete",
           () -> this.getProgressMeter().getSummary().complete(),
           MetricCategory.Core,
           "The current number of operations which have been completed"
       );
    }

    @Override
    public void initActivity() {
        super.initActivity();
        setDefaultsFromOpSequence(sequence);
    }


    public OpSequence<OpDispenser<? extends Op>> getOpSequence() {
        return sequence;
    }

//    /**
//     * When an adapter needs to identify an error uniquely for the purposes of
//     * routing it to the correct error handler, or naming it in logs, or naming
//     * metrics, override this method in your activity.
//     *
//     * @return A function that can reliably and safely map an instance of Throwable to a stable name.
//     */
//    @Override
//    public final Function<Throwable, String> getErrorNameMapper() {
//        return adapter.getErrorNameMapper();
//    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        for (DriverAdapter<?,?> adapter : adapters.values()) {
            if (adapter instanceof NBReconfigurable configurable) {
                NBConfigModel cfgModel = configurable.getReconfigModel();
                NBConfiguration cfg = cfgModel.matchConfig(activityDef.getParams());
                NBReconfigurable.applyMatching(cfg, List.of(configurable));
            }
        }
    }

//    @Override
//    public synchronized void onActivityDefUpdate(final ActivityDef activityDef) {
//        super.onActivityDefUpdate(activityDef);
//
//        for (final DriverAdapter adapter : this.adapters.values())
//            if (adapter instanceof NBReconfigurable reconfigurable) {
//                NBConfigModel cfgModel = reconfigurable.getReconfigModel();
//                final Optional<String> op_yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
//                if (op_yaml_loc.isPresent()) {
//                    final Map<String, Object> disposable = new LinkedHashMap<>(activityDef.getParams());
//                    final OpsDocList workload = OpsLoader.loadPath(op_yaml_loc.get(), disposable, "activities");
//                    cfgModel = cfgModel.add(workload.getConfigModel());
//                }
//                final NBConfiguration cfg = cfgModel.apply(activityDef.getParams());
//                reconfigurable.applyReconfig(cfg);
//            }
//
//    }

    @Override
    public List<OpTemplate> getSyntheticOpTemplates(OpsDocList opsDocList, Map<String, Object> cfg) {
        List<OpTemplate> opTemplates = new ArrayList<>();
        for (DriverAdapter<?,?> adapter : adapters.values()) {
            if (adapter instanceof SyntheticOpTemplateProvider sotp) {
                List<OpTemplate> newTemplates = sotp.getSyntheticOpTemplates(opsDocList, cfg);
                opTemplates.addAll(newTemplates);
            }
        }
        return opTemplates;
    }

    /**
     * This is done here since driver adapters are intended to keep all of their state within
     * dedicated <em>state space</em> types. Any space which implements {@link Shutdownable}
     * will be closed when this activity shuts down.
     */
    @Override
    public void shutdownActivity() {
        for (Map.Entry<String, DriverAdapter<?,?>> entry : adapters.entrySet()) {
            String adapterName = entry.getKey();
            DriverAdapter<?, ?> adapter = entry.getValue();
            adapter.getSpaceCache().getElements().forEach((spaceName, space) -> {
                if (space instanceof AutoCloseable autocloseable) {
                    try {
                        autocloseable.close();
                    } catch (Exception e) {
                        throw new RuntimeException("Error while shutting down state space for " +
                            "adapter=" + adapterName + ", space=" + spaceName + ": " + e, e);
                    }
                }
            });
        }
    }

    @Override
    public NBLabels getLabels() {
        return super.getLabels();
    }


    @Override
    public void onEvent(NBEvent event) {
        switch(event) {
            case ParamChange<?> pc -> {
                switch (pc.value()) {
                    case SetThreads st -> activityDef.setThreads(st.threads);
                    case CycleRateSpec crs -> createOrUpdateCycleLimiter(crs);
                    case StrideRateSpec srs -> createOrUpdateStrideLimiter(srs);
                    default -> super.onEvent(event);
                }
            }
            default -> super.onEvent(event);
        }
    }


}
