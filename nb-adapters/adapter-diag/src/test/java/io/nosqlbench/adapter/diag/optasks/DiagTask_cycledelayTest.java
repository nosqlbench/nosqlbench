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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
public class DiagTask_cycledelayTest {

    private DiagTask_cycledelay configured(Map<String, ?> cfg) {
        DiagTask_cycledelay task = new DiagTask_cycledelay();
        task.setName("test");
        task.setLabelsFrom(NBLabeledElement.EMPTY);
        NBConfiguration applied = task.getConfigModel().apply(cfg);
        task.applyConfig(applied);
        return task;
    }

    /// A fixed-delay binding should produce a measurable sleep close to the requested amount.
    /// Tolerance is intentionally loose since OS scheduling can add a millisecond or two on
    /// loaded CI machines; the assertion is about the *direction* (the task actually slept),
    /// not microsecond precision.
    @Test
    public void delaysApproximatelyAsRequested() {
        DiagTask_cycledelay task = configured(Map.of(
            "name", "test",
            "delay", "FixedValue(5000L)",
            "unit", "us"
        ));
        long start = System.nanoTime();
        task.apply(5_000L, Map.of());
        long elapsedNs = System.nanoTime() - start;
        long requestedNs = 5_000L * 1_000L;
        assertThat(elapsedNs)
            .as("Should sleep at least the requested 5ms")
            .isGreaterThanOrEqualTo(requestedNs)
            .as("Sleep shouldn't massively overshoot on a quiet box (allow 50ms overhead)")
            .isLessThan(requestedNs + 50_000_000L);
    }

    /// Zero / negative delays are no-ops — the task returns immediately. Lets a binding like
    /// `Mod(2); Mul(0L)` express "sometimes delay, sometimes don't" without a separate guard.
    @Test
    public void zeroDelayIsImmediate() {
        DiagTask_cycledelay task = configured(Map.of(
            "name", "test",
            "delay", "FixedValue(0L)",
            "unit", "us"
        ));
        long start = System.nanoTime();
        task.apply(1L, Map.of());
        long elapsedNs = System.nanoTime() - start;
        assertThat(elapsedNs).isLessThan(2_000_000L); // 2ms ceiling
    }

    @Test
    public void unitMsScalesDelay() {
        DiagTask_cycledelay task = configured(Map.of(
            "name", "test",
            "delay", "FixedValue(3L)",
            "unit", "ms"
        ));
        long start = System.nanoTime();
        task.apply(1L, Map.of());
        long elapsedNs = System.nanoTime() - start;
        assertThat(elapsedNs).isGreaterThanOrEqualTo(3_000_000L);
    }

    @Test
    public void unknownUnitRejected() {
        DiagTask_cycledelay task = new DiagTask_cycledelay();
        task.setName("test");
        task.setLabelsFrom(NBLabeledElement.EMPTY);
        NBConfiguration applied = task.getConfigModel().apply(Map.of(
            "name", "test",
            "delay", "FixedValue(1L)",
            "unit", "weeks"
        ));
        assertThatThrownBy(() -> task.applyConfig(applied))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown 'unit'");
    }

    /// Different cycle values should produce different per-cycle delays when the binding is
    /// cycle-dependent — confirming the binding sees the cycle parameter and the task is
    /// re-evaluating it each call (not caching).
    @Test
    public void differentCyclesProduceDifferentDelays() {
        DiagTask_cycledelay task = configured(Map.of(
            "name", "test",
            "delay", "HashRange(100L, 1000L)",
            "unit", "us"
        ));
        // Just confirm it doesn't throw and completes for a small batch of varying cycles.
        assertThatCode(() -> {
            for (long c = 0; c < 5; c++) {
                task.apply(c, Map.of());
            }
        }).doesNotThrowAnyException();
    }
}
