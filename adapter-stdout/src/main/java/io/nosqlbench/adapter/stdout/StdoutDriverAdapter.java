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

package io.nosqlbench.adapter.stdout;

import io.nosqlbench.adapters.api.activityconfig.yaml.OpData;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service(value = DriverAdapter.class, selector = "stdout")
public class StdoutDriverAdapter extends BaseDriverAdapter<StdoutOp, StdoutSpace> implements SyntheticOpTemplateProvider {
    private final static Logger logger = LogManager.getLogger(StdoutDriverAdapter.class);

    @Override
    public OpMapper<StdoutOp> getOpMapper() {
        DriverSpaceCache<? extends StdoutSpace> ctxCache = getSpaceCache();
        return new StdoutOpMapper(this, ctxCache);
    }

    @Override
    public Function<String, ? extends StdoutSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new StdoutSpace(cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(this.getClass())
            .add(super.getConfigModel())
            .add(StdoutSpace.getConfigModel());
    }

    @Override
    public List<OpTemplate> getSyntheticOpTemplates(OpsDocList opsDocList, Map<String, Object> cfg) {
        Set<String> activeBindingNames = new LinkedHashSet<>(opsDocList.getDocBindings().keySet());

        if (activeBindingNames.size()==0) {
            logger.warn("Unable to synthesize op for driver=" + this.getAdapterName() + " with zero bindings.");
            return List.of();
        }

        String bindings = Optional.ofNullable(cfg.get("bindings")).map(Object::toString).orElse("doc");
        Pattern bindingsFilter = Pattern.compile(bindings.equalsIgnoreCase("doc") ? ".*" : bindings);

        Set<String> filteredBindingNames = activeBindingNames
            .stream()
            .filter(n -> {
                if (bindingsFilter.matcher(n).matches()) {
                    logger.trace(() -> "bindings filter kept binding '" + n + "'");
                    return true;
                } else {
                    logger.trace(() -> "bindings filter removed binding '" + n + "'");
                    return false;
                }
            })
            .collect(Collectors.toSet());

        if (filteredBindingNames.size() == 0) {
            logger.warn("Unable to synthesize op for driver="+getAdapterName()+" when " + activeBindingNames.size()+"/"+activeBindingNames.size() + " bindings were filtered out with bindings=" + bindings);
            return List.of();
        }

        OpData op = new OpData("synthetic", "synthetic", Map.of(), opsDocList.getDocBindings(), cfg,
            Map.of("stmt", genStatementTemplate(filteredBindingNames, cfg)));

        return List.of(op);
    }

    private String genStatementTemplate(Set<String> keySet, Map<String, Object> cfg) {
        TemplateFormat format = Optional.ofNullable(cfg.get("format"))
            .map(Object::toString)
            .map(TemplateFormat::valueOf)
            .orElse(TemplateFormat.assignments);

        boolean ensureNewline = Optional.ofNullable(cfg.get("newline"))
            .map(Object::toString)
            .map(Boolean::valueOf)
            .orElse(true);

        String stmtTemplate = format.format(ensureNewline, new ArrayList<>(keySet));
        return stmtTemplate;
    }

}
