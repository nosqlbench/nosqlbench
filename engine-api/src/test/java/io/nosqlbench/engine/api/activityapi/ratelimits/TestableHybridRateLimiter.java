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

import io.nosqlbench.api.labels.NBLabeledElement;

import java.util.concurrent.atomic.AtomicLong;

public class TestableHybridRateLimiter extends HybridRateLimiter {

    private final AtomicLong clock;

    public TestableHybridRateLimiter(final AtomicLong clock, final RateSpec rateSpec, final NBLabeledElement def) {
        super(def, "test", rateSpec);
        this.applyRateSpec(rateSpec);
        this.setLabel("test");
        this.clock = clock;
        this.init(def);
    }

    public long setClock(final long newValue) {
        final long oldValue = this.clock.get();
        this.clock.set(newValue);
        return oldValue;
    }

    public long getClock() {
        return this.clock.get();
    }

    @Override
    protected long getNanoClockTime() {
        return this.clock.get();
    }

}
