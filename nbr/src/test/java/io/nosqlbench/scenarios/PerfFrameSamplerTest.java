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

package io.nosqlbench.scenarios;

import io.nosqlbench.scenarios.findmax.SimFrameCapture;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class PerfFrameSamplerTest {

    @Test
    public void testBasicValues() {
        SimFrameCapture pws = new SimFrameCapture();
        pws.addDirect("a",() -> 1.0d, 1.0d);
        pws.addDirect("b",()-> 3.0d, 3.0d);

        pws.startWindow();
        pws.stopWindow();
        double value = pws.getValue();
        assertThat(value).isCloseTo(9.0, Offset.offset(0.002));
    }

    @Test
    public void testDeltaValues() {
        AtomicLong a1 = new AtomicLong(0);
        DoubleSupplier ds1 = () -> (double) a1.get();

        AtomicLong a2 = new AtomicLong(0);
        DoubleSupplier ds2 = () -> (double) a2.get();

        SimFrameCapture pws = new SimFrameCapture();
        pws.addDeltaTime("a",ds1, 1.0d);
        pws.addDeltaTime("b",ds2, 1.0d);

        pws.startWindow(0L);

        a1.set(3L);
        a2.set(10L);

        pws.stopWindow(1000L);
        double value = pws.getValue();
        assertThat(value).isCloseTo(30.0,Offset.offset(0.001));

        pws.startWindow(10000L);
        a1.set(42); // 42-3=39
        a2.set(42); // 42-10=32

        pws.stopWindow(11000L);
        double value2 = pws.getValue();
        assertThat(value2).isCloseTo(1248.0,Offset.offset(0.001));

    }

}
