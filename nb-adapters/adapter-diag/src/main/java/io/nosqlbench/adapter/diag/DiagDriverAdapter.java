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

package io.nosqlbench.adapter.diag;


import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplates;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.params.NBParams;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.NBReconfigurable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;

@Service(value = DriverAdapter.class, selector = "diag")
public class DiagDriverAdapter extends BaseDriverAdapter<DiagOp, DiagSpace> implements SyntheticOpTemplateProvider {

    private final static Logger logger = LogManager.getLogger(DiagDriverAdapter.class);
    private DiagOpMapper mapper;


    public DiagDriverAdapter(NBComponent parentComponent, NBLabels labels) {
        super(parentComponent, labels);
        logger.debug("starting up");
    }

    @Override
    public synchronized OpMapper<DiagOp,DiagSpace> getOpMapper() {
        if (this.mapper == null) {
            this.mapper = new DiagOpMapper(this);
        }
        return this.mapper;
    }

    @Override
    public LongFunction<DiagSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (long name) -> new DiagSpace(this, name, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        NBConfigModel model = getConfigModel();
        model.add(DiagSpace.getStaticConfigModel());
        return model;
    }

    @Override
    public NBConfigModel getReconfigModel() {
        NBConfigModel model = getReconfigModel();
        NBConfigModel mapperModel = NBReconfigurable.collectModels(DiagDriverAdapter.class, List.of(mapper));
        return model.add(mapperModel);
    }

    @Override
    public List<Function<String, Optional<Map<String, Object>>>> getOpStmtRemappers() {
        return List.of(
            stmt -> {
                if (stmt.matches("^\\w+$")) {
                    return Optional.of(new LinkedHashMap<String, Object>(Map.of("type", stmt)));
                } else {
                    return Optional.empty();
                }
            },
            stmt -> Optional.of(NBParams.one(stmt).getMap())
        );
    }

    @Override
    public synchronized void applyConfig(NBConfiguration cfg) {
        super.applyConfig(cfg);
    }

    @Override
    public void applyReconfig(NBConfiguration cfg) {
        super.applyReconfig(cfg);
        NBReconfigurable.applyMatching(cfg, List.of(mapper));
    }

    @Override
    public OpTemplates getSyntheticOpTemplates(
        OpTemplates opTemplates,
        Map<String, Object> params) {
        OpTemplates matching = OpsLoader.loadString(
            "noop: noop", OpTemplateFormat.inline, params, null).getOps().matching("", true);
        return matching;
    }

}
