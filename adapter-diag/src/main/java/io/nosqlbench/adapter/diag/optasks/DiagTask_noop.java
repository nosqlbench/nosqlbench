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

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.nb.annotations.Service;

import java.util.Map;

@Service(value= DiagTask.class,selector = "noop")
public class DiagTask_noop extends BaseDiagTask {

    private String name;

    @Override
    public void applyConfig(NBConfiguration cfg) {
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_noop.class)
            .add(Param.required("name",String.class))
            .asReadOnly();
    }

    @Override
    public Map<String, Object> apply(Long aLong, Map<String, Object> stringObjectMap) {
        return Map.of();
    }

}
