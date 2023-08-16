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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;

public class CompoundCycleFunction<T> implements CycleFunction<T> {

    private final List<CycleFunction<T>> functions = new ArrayList<>();
    private final BinaryOperator<T> reducer;

    public CompoundCycleFunction(BinaryOperator<T> reducer, CycleFunction<T> ... functions) {
        this(reducer, Arrays.asList(functions));

    }
    public CompoundCycleFunction(BinaryOperator<T> reducer, List<CycleFunction<T>> functions) {
        this.functions.addAll(functions);
        this.reducer = reducer;
    }

    @Override
    public T apply(long value) {
        return functions.stream()
            .map(f -> f.apply(value))
            .reduce(reducer)
            .get();
    }

    @Override
    public CycleFunction<T> newInstance() {
        ArrayList<CycleFunction<T>> newFunctionList = new ArrayList<>(this.functions.size());
        for (CycleFunction<T> function : this.functions) {
            newFunctionList.add(function.newInstance());
        }
        return new CompoundCycleFunction<T>(reducer, newFunctionList);
    }

    @Override
    public String getExpressionDetails() {
        return String.join(
            ", ",
            functions.stream().map(f -> f.getExpressionDetails()).toList()
        );
    }

    @Override
    public <V> void setVariable(String name, V value) {
        for (CycleFunction<T> function : functions) {
            function.setVariable(name, value);
        }
    }
}
