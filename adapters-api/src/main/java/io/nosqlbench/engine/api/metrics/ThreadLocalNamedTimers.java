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

import com.codahale.metrics.Timer;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auxiliary thread-local metrics for an activity which are tracked by name.
 */
public class ThreadLocalNamedTimers {

    private final static Logger logger = LogManager.getLogger(ThreadLocalNamedTimers.class);

    public transient final static ThreadLocal<ThreadLocalNamedTimers> TL_INSTANCE = ThreadLocal.withInitial(ThreadLocalNamedTimers::new);
    private final static Map<String, Timer> timers = new HashMap<>();
    private final Map<String, Timer.Context> contexts = new HashMap<>();

    public static void addTimer(ActivityDef def, String name, int hdrdigits) {
        if (timers.containsKey("name")) {
            logger.warn("A timer named '" + name + "' was already defined and initialized.");
        }
        Timer timer = ActivityMetrics.timer(def, name, hdrdigits);
        timers.put(name, timer);
    }

    public static void addTimer(ParsedOp pop, String name) {
        if (timers.containsKey("name")) {
            logger.warn("A timer named '" + name + "' was already defined and initialized.");
        }
        Timer timer = ActivityMetrics.timer(pop.getStaticConfig("alias",String.class)+"."+name);
        timers.put(name, timer);
    }

    public void start(String name) {
        Timer.Context context = timers.get(name).time();
        contexts.put(name, context);
    }

    public void stop(String name) {
        Timer.Context context = contexts.get(name);
        context.stop();
    }

    public void start(List<String> timerNames) {
        for (String timerName : timerNames) {
            start(timerName);
        }
    }

    public void start(String[] timerNames) {
        for (String timerName : timerNames) {
            start(timerName);
        }
    }

    public void stop(List<String> timerName) {
        for (String stopTimer : timerName) {
            stop(stopTimer);
        }
    }

    public void stop(String[] timerStops) {
        for (String timerStop : timerStops) {
            stop(timerStop);
        }
    }
}
