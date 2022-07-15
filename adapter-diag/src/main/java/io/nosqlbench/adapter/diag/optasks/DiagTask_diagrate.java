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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiters;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateSpec;
import io.nosqlbench.nb.annotations.Service;

import java.util.Map;

@Service(value = DiagTask.class, selector = "diagrate")
public class DiagTask_diagrate implements DiagTask, NBReconfigurable {
    private String name;
    private RateLimiter rateLimiter;
    private RateSpec rateSpec;

    private void updateRateLimiter(String spec) {
        this.rateSpec = new RateSpec(spec);
        rateLimiter = RateLimiters.createOrUpdate(
            this,
            "diag",
            rateLimiter,
            rateSpec
        );
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_diagrate.class)
            .add(Param.required("diagrate", String.class))
            .add(Param.required("name", String.class))
            .asReadOnly();
    }

    @Override
    public NBConfigModel getReconfigModel() {
        return ConfigModel.of(DiagTask_diagrate.class)
            .add(Param.optional("diagrate"))
            .asReadOnly();
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        this.name = cfg.get("name", String.class);
        cfg.getOptional("diagrate").ifPresent(this::updateRateLimiter);
    }

    @Override
    public void applyReconfig(NBConfiguration recfg) {
        recfg.getOptional("diagrate").ifPresent(this::updateRateLimiter);
    }

    @Override
    public Map<String, Object> apply(Long aLong, Map<String, Object> stringObjectMap) {
        rateLimiter.maybeWaitForOp();
        return stringObjectMap;
    }


    @Override
    public String getName() {
        return name;
    }
}
