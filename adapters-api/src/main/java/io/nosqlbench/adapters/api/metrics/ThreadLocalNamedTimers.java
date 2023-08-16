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

package io.nosqlbench.adapters.api.metrics;

import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auxiliary thread-local metrics for an activity which are tracked by name.
 */
public class ThreadLocalNamedTimers {

    private static final Logger logger = LogManager.getLogger(ThreadLocalNamedTimers.class);

    public final static ThreadLocal<ThreadLocalNamedTimers> TL_INSTANCE = ThreadLocal.withInitial(ThreadLocalNamedTimers::new);
    private static final Map<String, Timer> timers = new HashMap<>();
    private final Map<String, Context> contexts = new HashMap<>();

    public static void addTimer(final ParsedOp pop, final String name) {
        if (ThreadLocalNamedTimers.timers.containsKey("name"))
            ThreadLocalNamedTimers.logger.warn("A timer named '{}' was already defined and initialized.", name);
        ThreadLocalNamedTimers.timers.put(name, ActivityMetrics.timer(pop,name,ActivityMetrics.DEFAULT_HDRDIGITS));
    }

    public void start(final String name) {
        final Context context = ThreadLocalNamedTimers.timers.get(name).time();
        this.contexts.put(name, context);
    }

    public void stop(final String name) {
        final Context context = this.contexts.get(name);
        context.stop();
    }

    public void start(final List<String> timerNames) {
        for (final String timerName : timerNames) this.start(timerName);
    }

    public void start(final String[] timerNames) {
        for (final String timerName : timerNames) this.start(timerName);
    }

    public void stop(final List<String> timerName) {
        for (final String stopTimer : timerName) this.stop(stopTimer);
    }

    public void stop(final String[] timerStops) {
        for (final String timerStop : timerStops) this.stop(timerStop);
    }
}
