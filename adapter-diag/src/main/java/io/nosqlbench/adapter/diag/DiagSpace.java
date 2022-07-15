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
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DiagSpace implements ActivityDefObserver {
    private final Logger logger = LogManager.getLogger(DiagSpace.class);

    private final NBConfiguration cfg;
    private final String name;
    private RateLimiter diagRateLimiter;
    private long interval;

    public DiagSpace(String name, NBConfiguration cfg) {
        this.cfg = cfg;
        this.name = name;
        logger.trace("diag space initialized as '" + name + "'");
    }

    public void applyConfig(NBConfiguration cfg) {
        this.interval = cfg.get("interval",long.class);
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagSpace.class)
            .add(Param.defaultTo("interval",1000))
            .asReadOnly();
    }

    public void maybeWaitForOp(double diagrate) {
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
