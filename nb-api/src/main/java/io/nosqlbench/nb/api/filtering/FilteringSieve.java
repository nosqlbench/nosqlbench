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

package io.nosqlbench.nb.api.filtering;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FilteringSieve<T> implements TristateFilter<T>{

    private final List<TristateFilter<T>> sieve;
    private final Policy defaultPolicy;

    public FilteringSieve(Policy defaultPolicy, List<TristateFilter<T>> filterPhases) {
        this.sieve = filterPhases;
        this.defaultPolicy = defaultPolicy;
    }

    @Override
    public Policy apply(T element) {
        for (TristateFilter<T> tTristateFilter : sieve) {
            Policy phaseResult = tTristateFilter.apply(element);
            if (phaseResult!=Policy.Ignore) return phaseResult;
        }
        return defaultPolicy;
    }

    public static class Builder<T> {
        private final List<TristateFilter<T>> phaseFilters = new ArrayList<>();
        private Policy defaultPolicy = Policy.Ignore;

        public Builder<T> keepByDefault() {
            this.defaultPolicy = Policy.Keep;
            return this;
        }
        public Builder<T> discardByDefault() {
            this.defaultPolicy = Policy.Discard;
            return this;
        }

        public Builder<T> withPhase(Predicate<T> predicate, Policy policy) {
            this.phaseFilters.add(new FilterPhase<>(predicate,policy));
            return this;
        }
        public Builder<T> withPhase(TristateFilter<T> phaseFilter) {
            this.phaseFilters.add(phaseFilter);
            return this;
        }

        public FilteringSieve<T> build() {
            return new FilteringSieve<>(defaultPolicy, phaseFilters);
        }
    }

    public List<T> filterList(ArrayList<T> input) {
        ArrayList<T> result = new ArrayList<T>();
        for (T in : input) if (apply(in).equals(TristateFilter.Policy.Keep)) result.add(in);

        return result;
    }

    public <U> Map<T,U> filterMapKeys(Map<T,U> input) {
        Map<T,U> result = new LinkedHashMap<T,U>();
        for (T k : input.keySet()) {
            if (apply(k).equals(Policy.Keep)) {
                result.put(k,input.get(k));
            }
        }
        return result;
    }
}
