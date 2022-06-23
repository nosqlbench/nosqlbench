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

package io.nosqlbench.adapter.stdout;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.OpTemplateSupplier;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service(value= DriverAdapter.class,selector = "stdout")
public class StdoutDriverAdapter extends BaseDriverAdapter<StdoutOp, StdoutSpace> implements OpTemplateSupplier, io.nosqlbench.engine.api.activityimpl.DefaultOpTemplateSupplier {

    @Override
    public OpMapper<StdoutOp> getOpMapper() {
        DriverSpaceCache<? extends StdoutSpace> ctxCache = getSpaceCache();
        return new StdoutOpMapper(ctxCache);
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
    public Optional<List<OpTemplate>> loadOpTemplates(NBConfiguration cfg) {
        throw new RuntimeException("implement me");
    }
}
