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

package io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ResultFilteringSieve implements TristateFilter<ResultReadable> {

    private final Policy defaultPolicy;
    private final List<TristateFilter<ResultReadable>> phases;

    private ResultFilteringSieve(Policy defaultPolicy, List<TristateFilter<ResultReadable>> phases) {
        this.defaultPolicy = defaultPolicy;
        this.phases = phases;
    }

    @Override
    public Policy apply(ResultReadable resultReadable) {
        Policy policy;
        for (TristateFilter<ResultReadable> phase : phases) {
            policy = phase.apply(resultReadable);
            if (policy!=Policy.Ignore) {
                return policy;
            }
        }
        policy=defaultPolicy;
        return policy;
    }

    public Predicate<ResultReadable> toExclusivePredicate() {
        return new ExclusiveFilterPredicate(this);
    }

    public Predicate<ResultReadable> toDefaultingPredicate(Policy defaultPolicy) {
        if (defaultPolicy==Policy.Discard) return toExclusivePredicate();
        return toInclusivePredicate();
    }


    private class InclusiveFilterPredicate implements Predicate<ResultReadable> {
        private final ResultFilteringSieve resultFilteringSieve;

        public InclusiveFilterPredicate(ResultFilteringSieve resultFilteringSieve) {
            this.resultFilteringSieve = resultFilteringSieve;
        }

        @Override
        public boolean test(ResultReadable cycleResult) {
            return resultFilteringSieve.apply(cycleResult)!=Policy.Discard;
        }
    }

    public Predicate<ResultReadable> toInclusivePredicate() {
        return new InclusiveFilterPredicate(this);
    }

    private static class ExclusiveFilterPredicate implements Predicate<ResultReadable> {

        private final ResultFilteringSieve sieve;

        public ExclusiveFilterPredicate(ResultFilteringSieve sieve) {
            this.sieve = sieve;
        }

        @Override
        public boolean test(ResultReadable cycleResult) {
            return sieve.apply(cycleResult)== Policy.Keep;
        }
    }

    public static class Builder {
        private final List<TristateFilter<ResultReadable>> phaseFilters = new ArrayList<>();
        private Policy defaultPolicy = Policy.Ignore;

        public Builder keepByDefault() {
            this.defaultPolicy = Policy.Keep;
            return this;
        }
        public Builder discardByDefault() {
            this.defaultPolicy = Policy.Discard;
            return this;
        }
        public Builder withPhase(TristateFilter<ResultReadable> phaseFilter) {
            this.phaseFilters.add(phaseFilter);
            return this;
        }
        public Builder include(int value) {
            return withPhase(new ResultFilterPhase(new CycleResultPredicate.ResultEquals(value),Policy.Keep));
        }
        public Builder exclude(int value) {
            return withPhase(new ResultFilterPhase(new CycleResultPredicate.ResultEquals(value),Policy.Discard));
        }
        public Builder include(int start, int end) {
            return withPhase(new ResultFilterPhase(new CycleResultPredicate.ResultInRange(start,end),Policy.Keep));
        }
        public Builder exclude(int start, int end) {
            return withPhase(new ResultFilterPhase(new CycleResultPredicate.ResultInRange(start,end),Policy.Discard));
        }

        public ResultFilteringSieve build() {
            return new ResultFilteringSieve(defaultPolicy, phaseFilters);
        }
    }
}
