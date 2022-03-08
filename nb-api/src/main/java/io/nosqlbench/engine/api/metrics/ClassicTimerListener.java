/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.ExponentiallyDecayingReservoir;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ClassicTimerListener extends CapabilityHook<TimerAttachment> {
    private final static Logger logger = LogManager.getLogger(ClassicTimerListener.class);

    private final MetricRegistry metricsRegistry;
    private final String sessionName;
    private final String prefix;
    private final Pattern pattern;
    private final String interval;
    private final TimeUnit nanoseconds;
    private final Map<String, Attachment> histos = new HashMap<>();

    public ClassicTimerListener(MetricRegistry metricRegistry, String sessionName, String prefix, Pattern pattern, String interval, TimeUnit nanoseconds) {
        this.metricsRegistry = metricRegistry;
        this.sessionName = sessionName;
        this.prefix = prefix;
        this.pattern = pattern;
        this.interval = interval;
        this.nanoseconds = nanoseconds;
    }

    @Override
    public void onCapableAdded(String name, TimerAttachment capable) {
        if (pattern.matcher(name).matches()) {
            String prefixed = prefix + "-" + name;
            Timer classicTimer = new Timer(new ExponentiallyDecayingReservoir());
            capable.attachTimer(classicTimer);

            this.histos.put(prefixed, new Attachment(name, prefix, capable, classicTimer));
            metricsRegistry.timer(prefixed, () -> classicTimer);
            logger.trace("Added classic timer attachment:" + prefixed);
        }


    }

    @Override
    public void onCapableRemoved(String name, TimerAttachment capable) {
        Attachment removed = histos.remove(name);
        logger.trace("Removed classic timer attachment: " + removed);
    }

    @Override
    protected Class<TimerAttachment> getCapabilityClass() {
        return TimerAttachment.class;
    }

    private static class Attachment {

        public final String upstreamName;
        public final TimerAttachment upstream;
        public Timer downstream;
        public String prefix;

        public Attachment(String upstreamName, String prefix, TimerAttachment upstream, Timer downstream) {
            this.prefix = prefix;
            this.upstreamName = upstreamName;
            this.upstream = upstream;
            this.downstream = downstream;
        }

        public String toString() {
            return "name:" + upstreamName + "->" + prefix + "-" + upstreamName;
        }

        public String getName() {
            return upstreamName;
        }
    }
}
