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

package io.nosqlbench.engine.api.activityapi.simrate;

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class ThreadLocalRateLimiters {

    private static final Logger logger = LogManager.getLogger(ThreadLocalRateLimiters.class);

    public static synchronized ThreadLocal<RateLimiter> createOrUpdate(
        final NBComponent parent,
        final ThreadLocal<RateLimiter> extantSource,
        final SimRateSpec spec
    ) {
        if (extantSource != null) {
            RateLimiter rl = extantSource.get();
            rl.applyRateSpec(spec);
            return extantSource;
        } else {
            Supplier<RateLimiter> rls;
            rls = switch (spec.getScope()) {
                case activity -> {
                    SimRate rl = new SimRate(parent, spec);
                    yield () -> rl;
                }
                case thread -> () -> new SimRate(
                    parent,
                    spec,
                    NBLabels.forKV("thread", Thread.currentThread().getName())
                );
            };
            return ThreadLocal.withInitial(rls);
        }
    }

}
