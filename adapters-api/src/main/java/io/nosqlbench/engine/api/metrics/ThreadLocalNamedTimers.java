package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
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

    public static void addTimer(ActivityDef def, String name) {
        if (timers.containsKey("name")) {
            logger.warn("A timer named '" + name + "' was already defined and initialized.");
        }
        Timer timer = ActivityMetrics.timer(def, name);
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

    public void start(List<String> timerName) {
        for (String startTimer : timerName) {
            start(startTimer);
        }
    }

    public void stop(List<String> timerName) {
        for (String stopTimer : timerName) {
            stop(stopTimer);
        }
    }

}
