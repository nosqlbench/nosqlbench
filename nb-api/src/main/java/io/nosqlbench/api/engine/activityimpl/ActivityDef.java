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

package io.nosqlbench.api.engine.activityimpl;

import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.engine.util.Unit;
import io.nosqlbench.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.Map;
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
public class ActivityDef implements NBNamedElement, NBLabeledElement {

    // milliseconds between cycles per thread, for slow tests only
    public static final String DEFAULT_ALIAS = "UNNAMEDACTIVITY";
    public static final String DEFAULT_ATYPE = "stdout  ";
    public static final String DEFAULT_CYCLES = "0";
    public static final int DEFAULT_THREADS = 1;
    private static final Logger logger = LogManager.getLogger(ActivityDef.class);
    // an alias with which to control the activity while it is running
    private static final String FIELD_ALIAS = "alias";
    // a file or URL containing the activity: op templates, generator bindings, ...
    private static final String FIELD_ATYPE = "type";
    // cycles for this activity in either "M" or "N..M" form. "M" form implies "0..M"
    private static final String FIELD_CYCLES = "cycles";
    // initial thread concurrency for this activity
    private static final String FIELD_THREADS = "threads";
    private static final String[] field_list = {
        ActivityDef.FIELD_ALIAS, ActivityDef.FIELD_ATYPE, ActivityDef.FIELD_CYCLES, ActivityDef.FIELD_THREADS
    };
    // parameter map has its own internal atomic map
    private final ParameterMap parameterMap;

    public ActivityDef(final ParameterMap parameterMap) {
        this.parameterMap = parameterMap;
    }

    public static Optional<ActivityDef> parseActivityDefOptionally(final String namedActivitySpec) {
        try {
            final ActivityDef activityDef = ActivityDef.parseActivityDef(namedActivitySpec);
            return Optional.of(activityDef);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    public static ActivityDef parseActivityDef(final String namedActivitySpec) {
        final Optional<ParameterMap> activityParameterMap = ParameterMap.parseParams(namedActivitySpec);
        final ActivityDef activityDef = new ActivityDef(activityParameterMap.orElseThrow(
                () -> new RuntimeException("Unable to parse:" + namedActivitySpec)
        ));
        ActivityDef.logger.info("parsed activityDef {} to-> {}", namedActivitySpec, activityDef);

        return activityDef;
    }

    public String toString() {
        return "ActivityDef:" + this.parameterMap.toString();
    }

    /**
     * The alias that the associated activity instance is known by.
     *
     * @return the alias
     */
    public String getAlias() {
        return this.parameterMap.getOptionalString("alias").orElse(ActivityDef.DEFAULT_ALIAS);
    }

    public String getActivityType() {
        return this.parameterMap.getOptionalString("type", "driver").orElse(ActivityDef.DEFAULT_ATYPE);
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
        final String cycles = this.parameterMap.getOptionalString("cycles").orElse(ActivityDef.DEFAULT_CYCLES);
        final int rangeAt = cycles.indexOf("..");
        final String startCycle;
        if (0 < rangeAt) startCycle = cycles.substring(0, rangeAt);
        else startCycle = "0";

        return Unit.longCountFor(startCycle).orElseThrow(
                () -> new RuntimeException("Unable to parse start cycles from " + startCycle)
        );
    }

    public void setStartCycle(final long startCycle) {
        this.parameterMap.set(ActivityDef.FIELD_CYCLES, startCycle + ".." + this.getEndCycle());
    }

    public void setStartCycle(final String startCycle) {
        this.setStartCycle(Unit.longCountFor(startCycle).orElseThrow(
                () -> new RuntimeException("Unable to convert start cycle '" + startCycle + "' to a value.")
        ));
    }

    public void setEndCycle(final String endCycle) {
        this.setEndCycle(Unit.longCountFor(endCycle).orElseThrow(
                () -> new RuntimeException("Unable to convert end cycle '" + endCycle + "' to a value.")
        ));
    }

    /**
     * The last cycle that will be used for execution of this activity, inclusive.
     *
     * @return the long end cycle
     */
    public long getEndCycle() {
        final String cycles = this.parameterMap.getOptionalString(ActivityDef.FIELD_CYCLES).orElse(ActivityDef.DEFAULT_CYCLES);
        final int rangeAt = cycles.indexOf("..");
        final String endCycle;
        if (0 < rangeAt) endCycle = cycles.substring(rangeAt + 2);
        else endCycle = cycles;
        return Unit.longCountFor(endCycle).orElseThrow(
                () -> new RuntimeException("Unable to convert end cycle from " + endCycle)
        );
    }

    public void setEndCycle(final long endCycle) {
        this.parameterMap.set(ActivityDef.FIELD_CYCLES, this.getStartCycle() + ".." + endCycle);
    }

    /**
     * The number of threads (AKA slots) that the associated activity should currently be using.
     *
     * @return target thread count
     */
    public int getThreads() {
        return this.parameterMap.getOptionalInteger(ActivityDef.FIELD_THREADS).orElse(ActivityDef.DEFAULT_THREADS);
    }

    public void setThreads(final int threads) {
        this.parameterMap.set(ActivityDef.FIELD_THREADS, threads);
    }

    /**
     * Get the parameter map, which is the backing-store for all data within an ActivityDef.
     *
     * @return the parameter map
     */
    public ParameterMap getParams() {
        return this.parameterMap;
    }

    public AtomicLong getChangeCounter() {
        return this.parameterMap.getChangeCounter();
    }

    public void setCycles(final String cycles) {
        this.parameterMap.set(ActivityDef.FIELD_CYCLES, cycles);
        this.checkInvariants();
    }

    public String getCycleSummary() {
        return "["
                + this.getStartCycle()
                + ".."
                + this.getEndCycle()
                + ")="
                + this.getCycleCount();
    }

    public long getCycleCount() {
        return this.getEndCycle() - this.getStartCycle();
    }

    private void checkInvariants() {
        if (this.getStartCycle() >= this.getEndCycle())
            throw new InvalidParameterException("Start cycle must be strictly less than end cycle, but they are [" + this.getStartCycle() + ',' + this.getEndCycle() + ')');
    }

    @Override
    public String getName() {
        return this.getAlias();
    }

    public ActivityDef deprecate(final String deprecatedName, final String newName) {
        final Object deprecatedParam = parameterMap.get(deprecatedName);
        if (null == deprecatedParam) return this;
        if (deprecatedParam instanceof CharSequence chars) {
            if (parameterMap.containsKey(newName))
                throw new BasicError("You have specified activity param '" + deprecatedName + "' in addition to the valid name '" + newName + "'. Remove '" + deprecatedName + "'.");
            ActivityDef.logger.warn("Auto replacing deprecated activity param '{}={}' with new '{}={}'.", deprecatedName, chars, newName, chars);
            this.parameterMap.put(newName, this.parameterMap.remove(deprecatedName));
        } else
            throw new BasicError("Can't replace deprecated name with value of type " + deprecatedName.getClass().getCanonicalName());
        return this;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of("alias", this.getAlias());
    }
}
