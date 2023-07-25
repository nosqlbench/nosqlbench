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

package io.nosqlbench.engine.api.util;

import io.nosqlbench.api.engine.util.Unit;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitParserTests {

    @Test
    public void testDoubleCountParser() {
        assertThat(Unit.doubleCountFor("1M")).isPresent().contains(1000000.0d);
        assertThat(Unit.convertDoubleCount(Unit.Count.KILO,"1M")).isPresent().contains(1000.0d);
        assertThat(Unit.convertDoubleCount(Unit.Count.MEGA, "1K")).isPresent().contains(0.001d);
    }

    @Test
    public void testLongCountParser() {
        assertThat(Unit.convertLongCount(Unit.Count.KILO, "1M")).isPresent().contains(1000L);
        assertThat(Unit.convertLongCount(Unit.Count.UNIT, "3074457344000000000")).isPresent().contains(3074457344000000000L);
        assertThat(Unit.convertLongCount(Unit.Count.UNIT, "3074457344000000020")).isPresent().contains(3074457344000000020L);
    }

    @Test
    public void testExponentialNotationDouble() {
        assertThat(Unit.doubleCountFor("1.0E10")).isPresent().contains(10000000000.0d);
    }

    @Test
    public void testPowerNotationDouble() {
        assertThat(Unit.doubleCountFor("2.7^10")).isPresent();
        assertThat(Unit.doubleCountFor("9.9^2").get()).isCloseTo(98.01,Offset.offset(0.1D));
    }

    @Test
    public void testExponentialNotationLong() {
        assertThat(Unit.longCountFor("1E9")).isPresent().contains((long)1E9);
        assertThat(Unit.longCountFor("10E9")).isPresent().contains((long)10E9);
    }

    @Test
    public void testDurationParser() {
        assertThat(Unit.msFor("1000")).contains(1000L);
        assertThat(Unit.msFor("1S")).contains(1000L);
        assertThat(Unit.msFor("1 SECOND")).contains(1000L);
        assertThat(Unit.msFor("5d")).contains((long)86400*1000*5);
        assertThat(Unit.durationFor(Unit.Duration.HOUR,"5 days")).contains(120L);
    }

    @Test
    public void testBytesParser() {
        assertThat(Unit.convertBytes(Unit.Bytes.KIB,"1 byte").get()).isCloseTo((1.0/1024.0), Offset.offset(0.000001D));
        assertThat(Unit.convertBytes(Unit.Bytes.GB,"1 megabyte").get()).isCloseTo((1/1000.0),Offset.offset(0.000001D));
        assertThat(Unit.convertBytes(Unit.Bytes.GB,"1 GiB").get())
                .isCloseTo(
                        ((1024.0D*1024.0D*1024.0D)/(1000.0D*1000.0D*1000.0D)),
                        Offset.offset(0.0000001D));
        assertThat(Unit.bytesFor("1.43 MB").get()).isCloseTo(1430000.0D,Offset.offset(0.00001D));
        assertThat(Unit.bytesFor("1KiB").get()).isCloseTo(1024.0D,Offset.offset(0.00001D));
    }

}
