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

import io.nosqlbench.api.config.standard.TestComponent;
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenPoolTest {

    ActivityDef adef = new ActivityDef(ParameterMap.parseOrException("alias=testing"));
    NBLabeledElement def = NBLabeledElement.forMap(this.adef.getParams().getStringStringMap());

    TestComponent component = new TestComponent("test","component");


    @Test
    public void testBackfillFullRate() {
        ThreadDrivenTokenPool p = new ThreadDrivenTokenPool(component,new RateSpec(10000000, 1.1), this.def);
        assertThat(p.refill(1000000L)).isEqualTo(1000000L);
        assertThat(p.getWaitPool()).isEqualTo(0L);
        assertThat(p.refill(100L)).isEqualTo(1000100);
        assertThat(p.getWaitPool()).isEqualTo(90L);
        assertThat(p.refill(10L)).isEqualTo(1000110L);
        assertThat(p.getWaitPool()).isEqualTo(99L);

        assertThat(p.refill(10)).isEqualTo(1000120L);
        assertThat(p.takeUpTo(100)).isEqualTo(100L);

    }
    @Test
    public void testTakeRanges() {
        ThreadDrivenTokenPool p = new ThreadDrivenTokenPool(component,new RateSpec(100, 10), def);
        p.refill(100);
        assertThat(p.takeUpTo(99)).isEqualTo(99L);
        assertThat(p.takeUpTo(10)).isEqualTo(1L);
        assertThat(p.takeUpTo(1L)).isEqualTo(0L);
    }

    @Test
    public void testChangedParameters() {

        RateSpec s1 = new RateSpec(1000L, 1.10D);
        ThreadDrivenTokenPool p = new ThreadDrivenTokenPool(component,s1, def);
        long r = p.refill(10000000);
        assertThat(r).isEqualTo(10000000L);
        assertThat(p.getWaitTime()).isEqualTo(10000000L);

        RateSpec s2 = new RateSpec(1000000L, 1.10D);
        p.apply(new NBLabeledElement() {
            @Override
            public NBLabels getLabels() {
                return NBLabels.forKV("name","test");
            }
        },s2);


    }
}
