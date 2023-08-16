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

package io.nosqlbench.engine.api.activityapi.cyclelog.tristate;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.MutableCycleResult;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.ResultFilteringSieve;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.tristate.TristateFilter;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultFilteringSieveTest {

    @Test
    public void testDefaultPolicy() {
        ResultFilteringSieve sieve = new ResultFilteringSieve.Builder().discardByDefault().build();
        assertThat(sieve.apply(new MutableCycleResult(4,5))).isEqualTo(TristateFilter.Policy.Discard);
    }

    @Test
    public void testBasicFilter() {
        ResultFilteringSieve sieve = new ResultFilteringSieve.Builder().discardByDefault().include(3).build();
        assertThat(sieve.apply(new MutableCycleResult(3L,2))).isEqualTo(TristateFilter.Policy.Discard);
        assertThat(sieve.apply(new MutableCycleResult(3L,3))).isEqualTo(TristateFilter.Policy.Keep);
    }

    @Test
    public void testRangeFilter() {
        ResultFilteringSieve sieve = new ResultFilteringSieve.Builder().keepByDefault().exclude(3,7).exclude(9).build();
        assertThat(sieve.apply(new MutableCycleResult(3L,2))).isEqualTo(TristateFilter.Policy.Keep);
        assertThat(sieve.apply(new MutableCycleResult(3L,3))).isEqualTo(TristateFilter.Policy.Discard);
        assertThat(sieve.apply(new MutableCycleResult(3L,7))).isEqualTo(TristateFilter.Policy.Discard);
        assertThat(sieve.apply(new MutableCycleResult(3L,8))).isEqualTo(TristateFilter.Policy.Keep);
        assertThat(sieve.apply(new MutableCycleResult(3L,9))).isEqualTo(TristateFilter.Policy.Discard);
        assertThat(sieve.apply(new MutableCycleResult(3L,10))).isEqualTo(TristateFilter.Policy.Keep);
    }

    @Test
    public void testPredicateConversions() {
        MutableCycleResult c1 = new MutableCycleResult(1,1);
        MutableCycleResult c2 = new MutableCycleResult(2,2);
        MutableCycleResult c3 = new MutableCycleResult(3,3);
        MutableCycleResult c4 = new MutableCycleResult(4,4);
        MutableCycleResult c5 = new MutableCycleResult(5,5);
        MutableCycleResult c6 = new MutableCycleResult(6,6);
        MutableCycleResult c7 = new MutableCycleResult(7,7);

        List<CycleResult> results = new ArrayList<CycleResult>() {
            {
                add(c1);
                add(c2);
                add(c3);
                add(c4);
                add(c5);
                add(c6);
                add(c7);
            }
        };

        Predicate<ResultReadable> includer = new ResultFilteringSieve.Builder().include(3).exclude(4).build().toInclusivePredicate();
        List<CycleResult> included = results.stream().filter(includer).collect(Collectors.toList());
        assertThat(included).containsExactly(c1,c2,c3,c5,c6,c7);

        Predicate<ResultReadable> excluder = new ResultFilteringSieve.Builder().include(3).exclude(4).build().toExclusivePredicate();
        List<CycleResult> excluded = results.stream().filter(excluder).collect(Collectors.toList());
        assertThat(excluded).containsExactly(c3);
    }


}
