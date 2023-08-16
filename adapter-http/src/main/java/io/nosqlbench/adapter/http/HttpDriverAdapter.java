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

package io.nosqlbench.adapter.http;

import io.nosqlbench.adapter.http.core.HttpFormatParser;
import io.nosqlbench.adapter.http.core.HttpOp;
import io.nosqlbench.adapter.http.core.HttpOpMapper;
import io.nosqlbench.adapter.http.core.HttpSpace;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "http")
public class HttpDriverAdapter extends BaseDriverAdapter<HttpOp, HttpSpace> {

    @Override
    public OpMapper<HttpOp> getOpMapper() {
        DriverSpaceCache<? extends HttpSpace> spaceCache = getSpaceCache();
        NBConfiguration config = getConfiguration();
        return new HttpOpMapper(this, config, spaceCache);
    }

    @Override
    public Function<String, ? extends HttpSpace> getSpaceInitializer(NBConfiguration cfg) {
        return spaceName -> new HttpSpace(spaceName, cfg);
    }

    @Override
    public List<Function<Map<String, Object>, Map<String, Object>>> getOpFieldRemappers() {
        return super.getOpFieldRemappers();
    }

    @Override
    public List<Function<String, Optional<Map<String, Object>>>> getOpStmtRemappers() {
        return List.of(
            s -> Optional.ofNullable(HttpFormatParser.parseUrl(s))
                .map(LinkedHashMap::new),
            s -> Optional.ofNullable(HttpFormatParser.parseInline(s))
                .map(LinkedHashMap::new),
            s -> Optional.ofNullable(HttpFormatParser.parseParams(s))
                .map(LinkedHashMap::new)
        );
    }

    @Override
    public NBConfigModel getConfigModel() {
        NBConfigModel thisCfgModel = ConfigModel.of(HttpDriverAdapter.class)
            .add(Param.defaultTo("enable_urlencode", false)
                .setDescription("Override auto-detection of URLENCODE[[ requirements in the uri field."))
            .asReadOnly();

        return super.getConfigModel().add(HttpSpace.getConfigModel()).add(thisCfgModel);
    }
}
