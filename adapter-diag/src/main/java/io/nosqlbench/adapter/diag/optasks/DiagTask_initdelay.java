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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;

import java.util.Map;

/**
 * Cause a blocking call to delay the initialization
 * of this owning operation for a number of milliseconds.
 */
@Service(value= DiagTask.class,selector = "initdelay")
public class DiagTask_initdelay extends BaseDiagTask {

    @Override
    public void applyConfig(NBConfiguration cfg) {
        long initdelay = cfg.get("initdelay",long.class);
        try {
            Thread.sleep(initdelay);
        } catch (InterruptedException ignored) {
        }

    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_initdelay.class)
            .add(Param.required("name",String.class))
            .add(Param.optional("initdelay",Long.class))
            .asReadOnly();
    }

    @Override
    public Map<String, Object> apply(Long aLong, Map<String, Object> stringObjectMap) {
        return Map.of();
    }
}
