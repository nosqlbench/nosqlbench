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

package io.nosqlbench.adapter.s4j.util;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class S4JAdapterMetrics implements NBLabeledElement {

    private static final Logger logger = LogManager.getLogger("S4JAdapterMetrics");

    private final String defaultAdapterMetricsPrefix;

    private Histogram messageSizeHistogram;
    private Timer bindTimer;
    private Timer executeTimer;

    public S4JAdapterMetrics(final String defaultMetricsPrefix) {
        defaultAdapterMetricsPrefix = defaultMetricsPrefix;
    }

    public String getName() {
        return "S4JAdapterMetrics";
    }

    public void initS4JAdapterInstrumentation() {
        // Histogram metrics
        messageSizeHistogram =
            ActivityMetrics.histogram(
                this,
                this.defaultAdapterMetricsPrefix + "message_size",
                ActivityMetrics.DEFAULT_HDRDIGITS);

        // Timer metrics
        bindTimer =
            ActivityMetrics.timer(
                this,
                this.defaultAdapterMetricsPrefix + "bind",
                ActivityMetrics.DEFAULT_HDRDIGITS);
        executeTimer =
            ActivityMetrics.timer(
                this,
                this.defaultAdapterMetricsPrefix + "execute",
                ActivityMetrics.DEFAULT_HDRDIGITS);
    }

    public Timer getBindTimer() { return this.bindTimer; }
    public Timer getExecuteTimer() { return this.executeTimer; }
    public Histogram getMessagesizeHistogram() { return this.messageSizeHistogram; }

    @Override
    public Map<String, String> getLabels() {
        return Map.of("name", this.getName());
    }
}
