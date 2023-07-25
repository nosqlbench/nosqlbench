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

package io.nosqlbench.adapters.api.evalcontext;

import java.util.List;
import java.util.function.BinaryOperator;

public class CycleFunctions {
    public static <T> CycleFunction<T> of(BinaryOperator<T> reducer, List<CycleFunction<T>> verifiers, T defaultResult) {
        if (verifiers.size()==0) {
            return new NOOPVerifier<>(defaultResult);
        } else if (verifiers.size()==1) {
            return verifiers.get(0);
        } else {
            return new CompoundCycleFunction<>(reducer, verifiers);
        }
    }

    public static class NOOPVerifier<V> implements CycleFunction<V> {
        private final V defaultResult;

        public NOOPVerifier(V defaultResult) {
            this.defaultResult = defaultResult;
        }

        @Override
        public V apply(long value) {
            return defaultResult;
        }

        @Override
        public CycleFunction<V> newInstance() {
            return new NOOPVerifier<>(defaultResult);
        }

        @Override
        public String getExpressionDetails() {
            return "return "+ defaultResult;
        }

        @Override
        public <V> void setVariable(String name, V value) {
        }
    }
}
