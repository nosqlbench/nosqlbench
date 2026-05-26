/*
 * Copyright (c) nosqlbench
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

/// Errors every Nth cycle in a deterministic, modulo-based pattern. Companion to
/// [DiagTask_erroroncycle], which matches one specific cycle by equality and is therefore
/// useless for generating a sustained error sprinkle.
///
/// **Parameter**:
/// - `modulo` (default 1000): error when `(cycle % modulo) == 0`. Set to 0 to disable.
///
/// Built for the synthetic dummy-session workload so that `errors_total`, `error_rate_*`,
/// and the success-vs-result-count gap are visibly non-zero. Composing this in a single op
/// alongside [DiagTask_cycledelay] means the same op's error rate is driven by the modulo
/// while its latency distribution is driven independently by the delay binding.
@Service(value = DiagTask.class, selector = "errormodulo")
public class DiagTask_errormodulo extends BaseDiagTask {

    private long modulo;

    @Override
    public void applyConfig(NBConfiguration cfg) {
        this.modulo = cfg.get("modulo", long.class);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_errormodulo.class)
            .add(Param.required("name", String.class))
            .add(Param.defaultTo("modulo", 1000L))
            .asReadOnly();
    }

    @Override
    public Map<String, Object> apply(Long cycle, Map<String, Object> opstate) {
        if (modulo > 0L && (cycle % modulo) == 0L) {
            throw new RuntimeException("synthetic-error: modulo=" + modulo + " cycle=" + cycle);
        }
        return opstate;
    }
}
