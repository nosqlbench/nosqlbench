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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.labels.NBLabeledElement;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DiagTask_gaugeTest {

    @Test
    public void testAvg() {
        DiagTask_gauge gaugeTask = new DiagTask_gauge();
        gaugeTask.setName("test");
        gaugeTask.setLabelsFrom(NBLabeledElement.EMPTY);
        NBConfiguration taskConfig = gaugeTask.getConfigModel().apply(Map.of(
                "name","test",
                "buckets","5",
                "binding", "Identity()"
        ));
        gaugeTask.applyConfig(taskConfig);
        for (long i = 0; i < 10; i++) {
            gaugeTask.apply(i,Map.of());
        }

        assertThat(gaugeTask.getValue()).isCloseTo(7.0d, Offset.offset(0.0001d));
    }

    @Test
    public void testMin() {
        DiagTask_gauge gaugeTask = new DiagTask_gauge();
        gaugeTask.setName("test");
        gaugeTask.setLabelsFrom(NBLabeledElement.EMPTY);
        NBConfiguration taskConfig = gaugeTask.getConfigModel().apply(Map.of(
            "name","test",
            "buckets","5",
            "stat", "min",
            "binding", "Identity()"
        ));
        gaugeTask.applyConfig(taskConfig);
        for (long i = 0; i < 10; i++) {
            gaugeTask.apply(i,Map.of());
        }

        assertThat(gaugeTask.getValue()).isCloseTo(5.0d, Offset.offset(0.0001d));
    }

    @Test
    public void testMax() {
        DiagTask_gauge gaugeTask = new DiagTask_gauge();
        gaugeTask.setName("test");
        gaugeTask.setLabelsFrom(NBLabeledElement.EMPTY);
        NBConfiguration taskConfig = gaugeTask.getConfigModel().apply(Map.of(
            "name","test",
            "buckets","5",
            "stat", "max",
            "binding", "Identity()"
        ));
        gaugeTask.applyConfig(taskConfig);
        for (long i = 0; i < 10; i++) {
            gaugeTask.apply(i,Map.of());
        }

        assertThat(gaugeTask.getValue()).isCloseTo(9.0d, Offset.offset(0.0001d));
    }

}
