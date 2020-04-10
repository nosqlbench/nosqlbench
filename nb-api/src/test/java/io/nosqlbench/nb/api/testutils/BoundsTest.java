/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.nb.api.testutils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BoundsTest {

    @Test
    public void testProgression2() {
        Bounds bounds = new Bounds(3000, 2);
        assertThat(bounds.getValue()).isEqualTo(3000L);
        assertThat(bounds.getNextValue()).isEqualTo(3500L);
        assertThat(bounds.getNextValue()).isEqualTo(4000L);
        assertThat(bounds.getNextValue()).isEqualTo(4500L);

        assertThat(bounds.setValue(9500).getNextValue()).isEqualTo(10000L);
        assertThat(bounds.getNextValue()).isEqualTo(15000L);
    }

    @Test
    public void testProgression1() {
        Bounds bounds = new Bounds(100, 1);
        assertThat(bounds.getValue()).isEqualTo(100L);
        assertThat(bounds.getNextValue()).isEqualTo(1000L);
        assertThat(bounds.getNextValue()).isEqualTo(10000L);
        assertThat(bounds.getNextValue()).isEqualTo(100000L);
        assertThat(bounds.getNextValue()).isEqualTo(1000000L);
    }


}
