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
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultValueFilterType;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.CoreResultValueFilter;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultFilterDispenser;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

public class CoreResultFilterTest {

    @Test
    public void testComponentLifecycle() {
        ResultValueFilterType filterType = CoreResultValueFilter.FINDER.get("core")
                .orElseThrow(() -> new RuntimeException(
                        "Unable to find " + ResultValueFilterType.class.getSimpleName() + " for 'core'"
                ));
        ResultFilterDispenser fd = filterType.getDispenser("in:5,ex:6,in:7");
        Predicate<ResultReadable> cycleResultFilter = fd.getResultFilter();
        assertThat(cycleResultFilter.test(new MutableCycleResult(3,3))).isFalse();
        assertThat(cycleResultFilter.test(new MutableCycleResult(3,5))).isTrue();
        assertThat(cycleResultFilter.test(new MutableCycleResult(3,6))).isFalse();
        assertThat(cycleResultFilter.test(new MutableCycleResult(3,7))).isTrue();
        assertThat(cycleResultFilter.test(new MutableCycleResult(3,8))).isFalse();

    }

}
