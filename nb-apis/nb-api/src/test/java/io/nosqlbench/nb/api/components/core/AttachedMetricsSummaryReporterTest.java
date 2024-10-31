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

package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.config.standard.TestComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBFunctionGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AttachedMetricsSummaryReporterTest {

    private final Logger logger = LogManager.getLogger(AttachedMetricsSummaryReporterTest.class);


    @Disabled
    @Test
    public void testSingleObjectScope() {
        try (TestComponent root = new TestComponent("root", "root")) {

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ignored) {
            }
            logger.debug("scope ending");
        }
    }


    // TODO: this output should also include the node itself
    // TODO: end lifecycle events need to be supported for metrics flushing


    @Disabled
    @Test
    public void testAttachedReporterScope() {
        try (NBComponentExecutionScope scope = new NBComponentExecutionScope()) {
            TestComponent root = new TestComponent("root", "root");
            scope.add(root);
            TestComponent l1 = new TestComponent(root, "l1", "l1");
            NBMetricCounter counter = l1.create().counter(
                "mycounter",
                MetricCategory.Verification,
                "A verification metric for testing"
            );
//            AttachedMetricsSummaryReporter reporter = l1.create().summaryReporter(1000);
            NBFunctionGauge g1 = root.create().gauge(
                "rootgauge", () -> 42d,
                MetricCategory.Verification,
                "A verification metric for testing"
            );
            NBFunctionGauge g2 = l1.create().gauge(
                "leafgauge", () -> 48d,
                MetricCategory.Verification,
                "A verification metric for testing"
            );

            // This wait state is here only to emulate some time passing while background processing
            // in the component hierarchy runs. Without it, you would be standing and immediate tearing
            // down the structure, which is not a realistic scenario, but is probably a meaningful
            // robustness test in and of itself
            try {
                Thread.sleep(3_000L);
            } catch (InterruptedException ignored) {
            }
            logger.debug("scope ending");
        }


        // TODO: this output should also include the node itself
        // TODO: end lifecycle events need to be supported for metrics flushing


    }

    @Test
    @Disabled
    public void testAttachedReporter() {
        TestComponent root = new TestComponent("root", "root");
        TestComponent l1 = new TestComponent(root, "l1", "l1");
        NBMetricCounter counter = l1.create().counter(
            "mycounter",
            MetricCategory.Verification,
            "A verification metric for testing"
        );
//        AttachedMetricsSummaryReporter reporter = l1.create().summaryReporter(5000);
        NBFunctionGauge g1 = root.create().gauge(
            "rootgauge", () -> 42d,
            MetricCategory.Verification,
            "A verification metric for testing"
        );
        NBFunctionGauge g2 = l1.create().gauge(
            "leafgauge", () -> 48d,
            MetricCategory.Verification,
            "A verification metric for testing"
        );

        // TODO: this output should also include the node itself
        // TODO: end lifecycle events need to be supported for metrics flushing

        try {
            Thread.sleep(2_000L);
        } catch (InterruptedException ignored) {
        }
//        reporter.close();
    }

}
