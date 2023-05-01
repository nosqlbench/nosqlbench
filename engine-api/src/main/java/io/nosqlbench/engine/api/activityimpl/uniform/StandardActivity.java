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

import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.errors.OpConfigError;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityconfig.OpsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.annotations.ServiceSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a typed activity which is expected to become the standard
 * core of all new activity types. Extant NB drivers should also migrate
 * to this when possible.
 *
 * @param <R> A type of runnable which wraps the operations for this type of driver.
 * @param <S> The context type for the activity, AKA the 'space' for a named driver instance and its associated object graph
 */
public class StandardActivity<R extends Op, S> extends SimpleActivity implements SyntheticOpTemplateProvider {
    private static final Logger logger = LogManager.getLogger("ACTIVITY");

    private final OpSequence<OpDispenser<? extends Op>> sequence;
    private final NBConfigModel yamlmodel;
    private final ConcurrentHashMap<String, DriverAdapter> adapters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, OpMapper<Op>> mappers = new ConcurrentHashMap<>();

    public StandardActivity(final ActivityDef activityDef, final NBLabeledElement parentLabels) {
        super(activityDef, parentLabels);
        final OpsDocList workload;

        final Optional<String> yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
        if (yaml_loc.isPresent()) {
            final Map<String, Object> disposable = new LinkedHashMap<>(activityDef.getParams());
            workload = OpsLoader.loadPath(yaml_loc.get(), disposable, "activities");
            this.yamlmodel = workload.getConfigModel();
        } else this.yamlmodel = ConfigModel.of(StandardActivity.class).asReadOnly();

        final ServiceLoader<DriverAdapter> adapterLoader = ServiceLoader.load(DriverAdapter.class);
        final Optional<String> defaultDriverName = activityDef.getParams().getOptionalString("driver");
        final Optional<DriverAdapter> defaultAdapter = defaultDriverName
            .flatMap(s -> ServiceSelector.of(s, adapterLoader).get());

        if (defaultDriverName.isPresent() && defaultAdapter.isEmpty())
            throw new BasicError("Unable to load default driver adapter '" + defaultDriverName.get() + '\'');

        // HERE, op templates are loaded before drivers are loaded
        final List<OpTemplate> opTemplates = this.loadOpTemplates(defaultAdapter);


        final List<ParsedOp> pops = new ArrayList<>();
        final List<DriverAdapter> adapterlist = new ArrayList<>();
        final NBConfigModel supersetConfig = ConfigModel.of(StandardActivity.class).add(this.yamlmodel);

        final Optional<String> defaultDriverOption = activityDef.getParams().getOptionalString("driver");
        for (final OpTemplate ot : opTemplates) {
            final ParsedOp incompleteOpDef = new ParsedOp(ot, NBConfiguration.empty(), List.of(), this);
            final String driverName = incompleteOpDef.takeOptionalStaticValue("driver", String.class)
                .or(() -> incompleteOpDef.takeOptionalStaticValue("type",String.class))
                .or(() -> defaultDriverOption)
                .orElseThrow(() -> new OpConfigError("Unable to identify driver name for op template:\n" + ot));

//            String driverName = ot.getOptionalStringParam("driver")
//                .or(() -> activityDef.getParams().getOptionalString("driver"))
//                .orElseThrow(() -> new OpConfigError("Unable to identify driver name for op template:\n" + ot));

            if (!this.adapters.containsKey(driverName)) {
                final DriverAdapter adapter = ServiceSelector.of(driverName, adapterLoader).get().orElseThrow(
                    () -> new OpConfigError("Unable to load driver adapter for name '" + driverName + '\'')
                );

                NBConfigModel combinedModel = this.yamlmodel;
                NBConfiguration combinedConfig = combinedModel.matchConfig(activityDef.getParams());

                if (adapter instanceof NBConfigurable configurable) {
                    final NBConfigModel adapterModel = configurable.getConfigModel();
                    supersetConfig.add(adapterModel);

                    combinedModel = adapterModel.add(this.yamlmodel);
                    combinedConfig = combinedModel.matchConfig(activityDef.getParams());
                    configurable.applyConfig(combinedConfig);
                }
                this.adapters.put(driverName, adapter);
                this.mappers.put(driverName, adapter.getOpMapper());
            }

            supersetConfig.assertValidConfig(activityDef.getParams().getStringStringMap());

            final DriverAdapter adapter = this.adapters.get(driverName);
            adapterlist.add(adapter);
            final ParsedOp pop = new ParsedOp(ot, adapter.getConfiguration(), List.of(adapter.getPreprocessor()), this);
            final Optional<String> discard = pop.takeOptionalStaticValue("driver", String.class);
            pops.add(pop);
        }

        if (defaultDriverOption.isPresent()) {
            final long matchingDefault = this.mappers.keySet().stream().filter(n -> n.equals(defaultDriverOption.get())).count();
            if (0 == matchingDefault)
                StandardActivity.logger.warn("All op templates used a different driver than the default '{}'", defaultDriverOption.get());
        }

        try {
            final boolean strict = activityDef.getParams().getOptionalBoolean("strict").orElse(false);
            this.sequence = this.createOpSourceFromParsedOps(this.adapters, this.mappers, adapterlist, pops);
        } catch (final Exception e) {
            if (e instanceof OpConfigError) throw e;
            throw new OpConfigError("Error mapping workload template to operations: " + e.getMessage(), null, e);
        }
    }

    @Override
    public void initActivity() {
        super.initActivity();
        this.setDefaultsFromOpSequence(this.sequence);
    }

    public OpSequence<OpDispenser<? extends Op>> getOpSequence() {
        return this.sequence;
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
    public synchronized void onActivityDefUpdate(final ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        for (final DriverAdapter adapter : this.adapters.values())
            if (adapter instanceof NBReconfigurable configurable) {
                final NBConfigModel cfgModel = configurable.getReconfigModel();
                final NBConfiguration cfg = cfgModel.matchConfig(activityDef.getParams());
                NBReconfigurable.applyMatching(cfg, List.of(configurable));
            }
    }

    @Override
    public List<OpTemplate> getSyntheticOpTemplates(final OpsDocList opsDocList, final Map<String, Object> cfg) {
        final List<OpTemplate> opTemplates = new ArrayList<>();
        for (final DriverAdapter adapter : this.adapters.values())
            if (adapter instanceof SyntheticOpTemplateProvider sotp) {
                final List<OpTemplate> newTemplates = sotp.getSyntheticOpTemplates(opsDocList, cfg);
                opTemplates.addAll(newTemplates);
            }
        return opTemplates;
    }

    /**
     * This is done here since driver adapters are intended to keep all of their state within
     * dedicated <em>state space</em> types. Any space which implements {@link io.nosqlbench.engine.api.activityapi.core.Shutdownable}
     * will be closed when this activity shuts down.
     */
    @Override
    public void shutdownActivity() {
        for (final Entry<String, DriverAdapter> entry : this.adapters.entrySet()) {
            final String adapterName = entry.getKey();
            final DriverAdapter<?,?> adapter = entry.getValue();
            adapter.getSpaceCache().getElements().forEach((spaceName, space) -> {
                if (space instanceof AutoCloseable autocloseable) try {
                    autocloseable.close();
                } catch (final Exception e) {
                    throw new RuntimeException("Error while shutting down state space for " +
                        "adapter=" + adapterName + ", space=" + spaceName + ": " + e, e);
                }
            });
        }
    }
}
