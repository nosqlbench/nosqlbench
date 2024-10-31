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

package io.nosqlbench.engine.core.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricMapTest {

    @Test
    public void testNodeByNodeConstruction() {
        MetricMap root = new MetricMap();
        MetricMap alpha = root.findOrCreateNodePath("alpha");
        MetricMap beta = alpha.findOrCreateNodePath("beta");
        MetricMap gamma = beta.findOrCreateNodePath("gamma");

        assertThat(root.containsKey("alpha")).isTrue();
        assertThat(root.findOrCreateNodePath("alpha","beta","gamma")==gamma).isTrue();
        assertThat(root.findOrCreateDottedNodePath("alpha.beta.gamma")==gamma).isTrue();
    }

    @Test
    public void testConcatenatedConstruction() {
        MetricMap root = new MetricMap();
        MetricMap gamma = root.findOrCreateDottedNodePath("alpha.beta.gamma");
        assertThat(root.findOrCreateDottedParentPath("alpha.beta.gamma.abstract_leaf_node")).isEqualTo(gamma);
        MetricMap alpha = root.findOrCreateDottedParentPath("alpha.beta");
        MetricMap beta = alpha.findOrCreateDottedParentPath("beta.gamma");
        MetricMap betaToo = beta.findOrCreateDottedParentPath("gamma");
        assertThat(beta).isEqualTo(betaToo);
    }


}
