package io.nosqlbench.virtdata.library.basics.shared.stateful;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class NullOrPassTest {

    @Test
    public void testRanging() {
        NullOrPass f = new NullOrPass(.10d, "value");
        SharedState.tl_ObjectMap.get().put("value",12345L);
        Object v = f.apply(2345L);
        assertThat(v).isOfAnyClassIn(Long.class);
        assertThat((Long)v).isEqualTo(2345L);
    }

    @Test
    public void testRatio100pct() {
        NullOrPass f = new NullOrPass(1.0,"value");
        NullOrPass g = new NullOrPass(0.0,"value");
    }

    @Test
    public void testLowRatio() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new NullOrPass(-0.00001d,"value"));
    }

    @Test
    public void testHighRatio() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new NullOrPass(1.000001d,"value"));
    }

}
