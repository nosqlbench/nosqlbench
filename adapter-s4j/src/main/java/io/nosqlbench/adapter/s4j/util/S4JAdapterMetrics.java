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
import io.nosqlbench.adapter.s4j.dispensers.S4JBaseOpDispenser;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class S4JAdapterMetrics  {

    private static final Logger logger = LogManager.getLogger("S4JAdapterMetrics");

    private final S4JBaseOpDispenser s4jBaseOpDispenser;

    private Histogram messageSizeHistogram;
    private Timer bindTimer;
    private Timer executeTimer;

    public S4JAdapterMetrics(final S4JBaseOpDispenser s4jBaseOpDispenser) {
        this.s4jBaseOpDispenser = s4jBaseOpDispenser;
    }

    public void initS4JAdapterInstrumentation() {
        // Histogram metrics
        this.messageSizeHistogram = s4jBaseOpDispenser.create().histogram(
            "s4j_message_size",
            MetricCategory.Driver,
            "S4J message size"
        );

        // Timer metrics
        this.bindTimer = s4jBaseOpDispenser.create().timer(
            "s4j_bind",
            MetricCategory.Driver,
            "S4J bind timer"
        );
        this.executeTimer = s4jBaseOpDispenser.create().timer(
            "s4j_execute",
            MetricCategory.Driver,
            "S4j execut timer"
        );
    }

    public Timer getBindTimer() { return bindTimer; }
    public Timer getExecuteTimer() { return executeTimer; }
    public Histogram getMessagesizeHistogram() { return messageSizeHistogram; }
}
