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

package io.nosqlbench.adapter.diag;

import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

public class DiagSpace implements ActivityDefObserver {
    private final NBConfiguration cfg;
    private RateLimiter diagRateLimiter;

    public DiagSpace(NBConfiguration cfg) {
        this.cfg = cfg;
    }

    public void applyConfig(NBConfiguration cfg) {

    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagSpace.class)
            .asReadOnly();
    }

    public boolean isLogCycle() {
        return cfg.getOrDefault("logcycle",false);
    }

    public void maybeWaitForOp() {
        if (diagRateLimiter != null) {
            long waittime = diagRateLimiter.maybeWaitForOp();
        }
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        NBConfiguration cfg = getConfigModel().apply(activityDef.getParams().getStringStringMap());
        this.applyConfig(cfg);
    }
}
