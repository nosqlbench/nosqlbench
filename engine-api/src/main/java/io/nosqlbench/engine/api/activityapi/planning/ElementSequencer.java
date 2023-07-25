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

package io.nosqlbench.engine.api.activityapi.planning;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public interface ElementSequencer<T> {

    int[] seqIndexByRatioFunc(List<T> elems, ToLongFunction<T> ratioFunc);
    int[] seqIndexesByRatios(List<T> elems, List<Long> ratios);

    default List<T> seqElementsByRatioFunc(List<T> elems, ToLongFunction<T> ratioFunc) {
        int[] ints = seqIndexByRatioFunc(elems, ratioFunc);
        return Arrays.stream(ints).mapToObj(elems::get).collect(Collectors.toList());
    }

    default String sequenceSummary(List<T> elems, ToLongFunction<T> ratioFunc, String delim) {
        return seqElementsByRatioFunc(elems,ratioFunc)
                .stream().map(String::valueOf).collect(Collectors.joining(delim));
    }

}
