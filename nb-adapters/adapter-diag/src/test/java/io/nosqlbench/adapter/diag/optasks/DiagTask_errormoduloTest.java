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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
public class DiagTask_errormoduloTest {

    private DiagTask_errormodulo configured(long modulo) {
        DiagTask_errormodulo task = new DiagTask_errormodulo();
        task.setName("test");
        task.setLabelsFrom(NBLabeledElement.EMPTY);
        NBConfiguration cfg = task.getConfigModel().apply(Map.of("name", "test", "modulo", modulo));
        task.applyConfig(cfg);
        return task;
    }

    @Test
    public void errorsOnZeroAndMultiples() {
        DiagTask_errormodulo task = configured(100L);
        for (long cycle : new long[]{0L, 100L, 200L, 1000L}) {
            assertThatThrownBy(() -> task.apply(cycle, Map.of()))
                .as("expected error on cycle %d (modulo 100)", cycle)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("synthetic-error");
        }
    }

    @Test
    public void doesNotErrorBetweenMultiples() {
        DiagTask_errormodulo task = configured(100L);
        for (long cycle : new long[]{1L, 50L, 99L, 101L, 199L}) {
            assertThatCode(() -> task.apply(cycle, Map.of()))
                .as("should not error on cycle %d (modulo 100)", cycle)
                .doesNotThrowAnyException();
        }
    }

    @Test
    public void moduloZeroDisablesErrors() {
        DiagTask_errormodulo task = configured(0L);
        for (long cycle = 0; cycle < 100; cycle++) {
            long c = cycle;
            assertThatCode(() -> task.apply(c, Map.of()))
                .as("modulo=0 must never error (cycle %d)", c)
                .doesNotThrowAnyException();
        }
    }
}
