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

package io.nosqlbench.adapter.venice.util;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.venice.dispensers.VeniceBaseOpDispenser;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VeniceAdapterMetrics {

    private static final Logger logger = LogManager.getLogger("VeniceAdapterMetrics");

    private Timer executeTimer;

    private Counter foundCounter;
    private Counter notFoundCounter;

    private final VeniceBaseOpDispenser veniceBaseOpDispenser;

    public VeniceAdapterMetrics(VeniceBaseOpDispenser veniceBaseOpDispenser) {
        this.veniceBaseOpDispenser = veniceBaseOpDispenser;
    }

    public String getName() {
        return "VeniceAdapterMetrics";
    }

    public void initVeniceAdapterInstrumentation() {

        this.executeTimer =
            ActivityMetrics.timer(
                veniceBaseOpDispenser,"execute",
                ActivityMetrics.DEFAULT_HDRDIGITS);


        this.foundCounter =
            ActivityMetrics.counter(
                veniceBaseOpDispenser,"found");

        this.notFoundCounter =
            ActivityMetrics.counter(
                veniceBaseOpDispenser, "notFound");
    }

    public Timer getExecuteTimer() { return executeTimer; }

    public Counter getFoundCounter() {
        return foundCounter;
    }

    public Counter getNotFoundCounter() {
        return notFoundCounter;
    }

}
