/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.labels;

import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class NBLabelsRenderingTest {

    @Test
    public void testLinearizeDot() {
        var l1 = NBLabels.forKV("a","b","c","d","e","f");
        String al1 = l1.linearize("a");
        assertThat(al1).isEqualTo("b{c=\"d\",e=\"f\"}");
        String al2 = l1.linearizeValues(',');
        assertThat(al2).isEqualTo("b,d,f");
        String al3 = l1.linearizeValues("a","c");
        assertThat(al3).isEqualTo("b.d");
    }

    @Test
    public void testLinearizeOpenMetricsFormat() {
        var l1 = NBLabels.forKV("a","b","c","d","e","f");
        String oml1 = l1.linearizeAsMetrics();
        assertThat(oml1).isEqualTo("{a=\"b\",c=\"d\",e=\"f\"}");
    }

}
