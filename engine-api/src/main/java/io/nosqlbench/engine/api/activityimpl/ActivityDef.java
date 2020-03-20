/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.util.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>A runtime definition for an activity.</p>
 * <p>Instances of ActivityDef hold control values for the execution of a single activity.
 * Each thread of the related activity is initialized with the associated ActivityDef.
 * When the ActivityDef is modified, interested activity threads are notified so that
 * they can dynamically adjust.</p>
 * <p>The canonical values for all parameters are kept internally in the parameter map.
 * Essentially, ActivityDef is just a type-aware wrapper around a thread-safe parameter map,
 * with an atomic change counter which can be used to signal changes to observers.</p>
 */
public class ActivityDef {

    // milliseconds between cycles per thread, for slow tests only
    public static final String DEFAULT_ALIAS = "ALIAS_UNSET";
    public static final String DEFAULT_ATYPE = "TYPE_UNSET";
    public static final String DEFAULT_CYCLES = "0";
    public static final int DEFAULT_THREADS = 1;
    private final static Logger logger = LoggerFactory.getLogger(ActivityDef.class);
    // an alias with which to control the activity while it is running
    private static final String FIELD_ALIAS = "alias";
    // a file or URL containing the activity: statements, generator bindings, ...
    private static final String FIELD_ATYPE = "type";
    // cycles for this activity in either "M" or "N..M" form. "M" form implies "0..M"
    private static final String FIELD_CYCLES = "cycles";
    // initial thread concurrency for this activity
    private static final String FIELD_THREADS = "threads";
    private static String[] field_list = new String[]{
            FIELD_ALIAS, FIELD_ATYPE, FIELD_CYCLES, FIELD_THREADS
    };
    // parameter map has its own internal atomic map
    private ParameterMap parameterMap;

    public ActivityDef(ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public static Optional<ActivityDef> parseActivityDefOptionally(String namedActivitySpec) {
        try {
            ActivityDef activityDef = parseActivityDef(namedActivitySpec);
            return Optional.of(activityDef);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static ActivityDef parseActivityDef(String namedActivitySpec) {
        Optional<ParameterMap> activityParameterMap = ParameterMap.parseParams(namedActivitySpec);
        ActivityDef activityDef = new ActivityDef(activityParameterMap.orElseThrow(
                () -> new RuntimeException("Unable to parse:" + namedActivitySpec)
        ));
        logger.debug("parsed activityDef " + namedActivitySpec + " to-> " + activityDef);

        return activityDef;
    }

    public String toString() {
        return "ActivityDef:" + parameterMap.toString();
    }

    /**
     * The alias that the associated activity instance is known by.
     *
     * @return the alias
     */
    public String getAlias() {
        return parameterMap.getOptionalString("alias").orElse(DEFAULT_ALIAS);
    }

    public String getActivityType() {
        return parameterMap.getOptionalString("type", "driver").orElse(DEFAULT_ATYPE);
    }

    /**
     * The first cycle that will be used for execution of this activity, inclusive.
     * If the value is provided as a range as in 0..10, then the first number is the start cycle
     * and the second number is the end cycle +1. Effectively, cycle ranges
     * are [closed,open) intervals, as in [min..max)
     *
     * @return the long start cycle
     */
    public long getStartCycle() {
        String cycles = parameterMap.getOptionalString("cycles").orElse(DEFAULT_CYCLES);
        int rangeAt = cycles.indexOf("..");
        String startCycle;
        if (rangeAt > 0) {
            startCycle = cycles.substring(0, rangeAt);
        } else {
            startCycle = "0";
        }

        return Unit.longCountFor(startCycle).orElseThrow(
                () -> new RuntimeException("Unable to parse start cycles from " + startCycle)
        );
    }

    public void setStartCycle(long startCycle) {
        parameterMap.set(FIELD_CYCLES, "" + startCycle + ".." + getEndCycle());
    }

    public void setStartCycle(String startCycle) {
        setStartCycle(Unit.longCountFor(startCycle).orElseThrow(
                () -> new RuntimeException("Unable to convert start cycle '" + startCycle + "' to a value.")
        ));
    }

    public void setEndCycle(String endCycle) {
        setEndCycle(Unit.longCountFor(endCycle).orElseThrow(
                () -> new RuntimeException("Unable to convert end cycle '" + endCycle + "' to a value.")
        ));
    }

    /**
     * The last cycle that will be used for execution of this activity, inclusive.
     *
     * @return the long end cycle
     */
    public long getEndCycle() {
        String cycles = parameterMap.getOptionalString(FIELD_CYCLES).orElse(DEFAULT_CYCLES);
        int rangeAt = cycles.indexOf("..");
        String endCycle;
        if (rangeAt > 0) {
            endCycle = cycles.substring(rangeAt + 2);
        } else {
            endCycle = cycles;
        }
        return Unit.longCountFor(endCycle).orElseThrow(
                () -> new RuntimeException("Unable to convert end cycle from " + endCycle)
        );
    }

    public void setEndCycle(long endCycle) {
        parameterMap.set(FIELD_CYCLES, "" + getStartCycle() + ".." + endCycle);
    }

    /**
     * The number of threads (AKA slots) that the associated activity should currently be using.
     *
     * @return target thread count
     */
    public int getThreads() {
        return parameterMap.getOptionalInteger(FIELD_THREADS).orElse(DEFAULT_THREADS);
    }

    public void setThreads(int threads) {
        parameterMap.set(FIELD_THREADS, threads);
    }

    /**
     * Get the parameter map, which is the backing-store for all data within an ActivityDef.
     *
     * @return the parameter map
     */
    public ParameterMap getParams() {
        return parameterMap;
    }

    public AtomicLong getChangeCounter() {
        return parameterMap.getChangeCounter();
    }

    public void setCycles(String cycles) {
        parameterMap.set(FIELD_CYCLES, cycles);
        checkInvariants();
    }

    public String getCycleSummary() {
        return "["
                + getStartCycle()
                + ".."
                + getEndCycle()
                + ")="
                + getCycleCount();
    }

    public long getCycleCount() {
        return (getEndCycle() - getStartCycle());
    }

    private void checkInvariants() {
        if (getStartCycle() >= getEndCycle()) {
            throw new InvalidParameterException("Start cycle must be strictly less than end cycle, but they are [" + getStartCycle() + "," + getEndCycle() + ")");
        }
    }
}
