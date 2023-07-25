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

package io.nosqlbench.adapter.tcpclient;

import io.nosqlbench.adapter.stdout.StdoutDriverAdapter;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

@Service(value= DriverAdapter.class,selector = "tcpclient")
public class TcpClientDriverAdapter extends BaseDriverAdapter<TcpClientOp, TcpClientAdapterSpace> implements SyntheticOpTemplateProvider {
    private final static Logger logger = LogManager.getLogger(TcpClientDriverAdapter.class);

    private final static StdoutDriverAdapter adap = new StdoutDriverAdapter();
    @Override
    public OpMapper<TcpClientOp> getOpMapper() {
        DriverSpaceCache<? extends TcpClientAdapterSpace> ctxCache = getSpaceCache();
        return new TcpClientOpMapper(this,ctxCache);
    }

    @Override
    public Function<String, ? extends TcpClientAdapterSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new TcpClientAdapterSpace(cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(this.getClass())
            .add(super.getConfigModel())
            .add(TcpClientAdapterSpace.getConfigModel());
    }

    @Override
    public List<OpTemplate> getSyntheticOpTemplates(OpsDocList opsDocList, Map<String,Object> cfg) {
        return adap.getSyntheticOpTemplates(opsDocList, cfg);
    }

}
