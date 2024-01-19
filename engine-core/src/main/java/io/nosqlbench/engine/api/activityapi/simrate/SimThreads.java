/*
 * Copyright (c) 2023 nosqlbench
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

import java.util.concurrent.Semaphore;
import java.util.function.Function;

public class SimThreads {

    private final Semaphore semaphore = new Semaphore(Integer.MAX_VALUE, true) {{
        try {
            acquire(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }};
    private boolean running = true;

    private Spec params = new Spec(0);

    public SimThreads(Spec params) {
        applySpec(params);
    }
    SimThreads(String concurrency) {
        this(new Spec(Integer.parseInt(concurrency)));
    }
    public SimThreads applySpec(Spec newSpec) {
        if (newSpec.equals(this.params)) {
            return this;
        }
        int effectiveConcurrency = this.params.concurrency();

        // releasing slots is uncontended
        while (newSpec.concurrency() > effectiveConcurrency) {
            int diff = newSpec.concurrency() - effectiveConcurrency;
            semaphore.release(diff);
            effectiveConcurrency+=diff;
        }
        // acquiring (locking out) slots is contended
        while (newSpec.concurrency() < effectiveConcurrency) {
            try {
                semaphore.acquire();
                effectiveConcurrency+=1;
            } catch (InterruptedException ignored) {
            }
        }
        this.params = newSpec;
        return this;
    }

    public <U,V> Function<U,V> wrap(Function<U,V> f) {
        return new Wrapper<>(this, f);
    }
    public static record Spec(int concurrency) {
    }

    public static class Wrapper<I,O> implements Function<I,O> {
        private final Function<I, O> function;
        private final SimThreads simThreads;

        public Wrapper(SimThreads simThreads, Function<I,O> function) {
            this.function = function;
            this.simThreads = simThreads;
        }
        @Override
        public O apply(I i) {
            try {
                simThreads.semaphore.acquire();
                return function.apply(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                simThreads.semaphore.release();
            }
        }
    }

    @Override
    public String toString() {
        return "concurrency " + (this.params.concurrency-this.semaphore.availablePermits()) + " / " + this.params.concurrency;
    }
}
