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

package io.nosqlbench.engine.api.activityapi.ratelimits;

import com.codahale.metrics.Gauge;
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.components.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum RateLimiters {
    ;
    private static final Logger logger = LogManager.getLogger(RateLimiters.class);

    public static synchronized RateLimiter createOrUpdate(final NBComponent parent, final String label, final RateLimiter extant, final RateSpec spec) {

        if (null == extant) {
            final RateLimiter rateLimiter= new HybridRateLimiter(parent, label, spec);

            RateLimiters.logger.info(() -> "Using rate limiter: " + rateLimiter);
            return rateLimiter;
        }
        extant.applyRateSpec(spec);
        RateLimiters.logger.info(() -> "Updated rate limiter: " + extant);
        return extant;
    }

    public static synchronized RateLimiter create(final NBComponent def, final String label, final String specString) {
        return RateLimiters.createOrUpdate(def, label, null, new RateSpec(specString));
    }

    public static class WaitTimeGauge implements Gauge<Double> {

        private final RateLimiter rateLimiter;

        public WaitTimeGauge(final RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }

        @Override
        public Double getValue() {
            return (double)this.rateLimiter.getTotalWaitTime();
        }
    }

    public static class RateGauge implements Gauge<Double> {
        private final RateLimiter rateLimiter;

        public RateGauge(final RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }

        @Override
        public Double getValue() {
            return this.rateLimiter.getRateSpec().opsPerSec;
        }
    }

    public static class BurstRateGauge implements Gauge<Double> {
        private final RateLimiter rateLimiter;

        public BurstRateGauge(final RateLimiter rateLimiter) {
            this.rateLimiter = rateLimiter;
        }

        @Override
        public Double getValue() {
            return this.rateLimiter.getRateSpec().getBurstRatio() * this.rateLimiter.getRateSpec().getRate();
        }
    }

}
