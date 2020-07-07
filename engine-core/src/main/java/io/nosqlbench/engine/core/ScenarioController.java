/*
 *   Copyright 2015 jshook
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.nosqlbench.engine.core;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityapi.core.ProgressMeter;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A ScenarioController provides a way to start Activities, modify them while running, and forceStopMotors, pause or restart them.
 */
public class ScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);
    private static final Logger scenariologger = LoggerFactory.getLogger("SCENARIO");

    private final Map<String, ActivityExecutor> activityExecutors = new ConcurrentHashMap<>();

    /**
     * Start an activity, given the activity definition for it. The activity will be known in the scenario
     * by the alias parameter.
     *
     * @param activityDef string in alias=value1;driver=value2;... format
     */
    public synchronized void start(ActivityDef activityDef) {
        ActivityExecutor activityExecutor = getActivityExecutor(activityDef, true);
        scenariologger.debug("START " + activityDef.getAlias());
        activityExecutor.startActivity();
    }

    /**
     * Start an activity, given a map which holds the activity definition for it. The activity will be known in
     * the scenario by the alias parameter.
     *
     * @param activityDefMap A map containing the activity definition
     */
    public synchronized void start(Map<String, String> activityDefMap) {
        ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        start(ad);
    }

    /**
     * Start an activity, given the name by which it is known already in the scenario. This is useful if you have
     * stopped an activity and want to start it again.
     *
     * @param alias the alias of an activity that is already known to the scenario
     */
    public synchronized void start(String alias) {
        start(ActivityDef.parseActivityDef(alias));
    }

    public synchronized void run(int timeout, Map<String, String> activityDefMap) {
        ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        run(timeout, ad);
    }

    /**
     * Synchronously run the defined activity with a timeout in seconds.
     *
     * @param timeout     seconds to await completion of the activity.
     * @param activityDef A definition for an activity to run
     */
    public synchronized void run(int timeout, ActivityDef activityDef) {
        ActivityExecutor activityExecutor = getActivityExecutor(activityDef, true);
        scenariologger.debug("RUN alias=" + activityDef.getAlias());
        scenariologger.debug(" (RUN/START) alias=" + activityDef.getAlias());
        activityExecutor.startActivity();
        scenariologger.debug(" (RUN/AWAIT before) alias=" + activityDef.getAlias());
        boolean completed = activityExecutor.awaitCompletion(timeout);
        scenariologger.debug(" (RUN/AWAIT after) completed=" + activityDef.getAlias());
    }

    public synchronized void run(int timeout, String activityDefString) {
        ActivityDef activityDef = ActivityDef.parseActivityDef(activityDefString);
        run(timeout, activityDef);
    }

    public synchronized void run(Map<String, String> activityDefMap) {
        run(Integer.MAX_VALUE, activityDefMap);
    }

    public synchronized void run(String activityDefString) {
        run(Integer.MAX_VALUE, activityDefString);
    }


    public synchronized void run(ActivityDef activityDef) {
        run(Integer.MAX_VALUE, activityDef);
    }


    public boolean isRunningActivity(String alias) {
        return isRunningActivity(aliasToDef(alias));
    }

    public boolean isRunningActivity(ActivityDef activityDef) {

        ActivityExecutor activityExecutor = getActivityExecutor(activityDef, false);
        return activityExecutor != null && activityExecutor.isRunning();
    }

    public boolean isRunningActivity(Map<String, String> activityDefMap) {
        ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        return isRunningActivity(ad);
    }

    /**
     * <p>Stop an activity, given an activity def. The only part of the activity def that is important is the
     * alias parameter. This method retains the activity def signature to provide convenience for scripting.</p>
     * <p>For example, sc.stop("alias=foo")</p>
     *
     * @param activityDef An activity def, including at least the alias parameter.
     */
    public synchronized void stop(ActivityDef activityDef) {
        ActivityExecutor activityExecutor = getActivityExecutor(activityDef, false);
        if (activityExecutor == null) {
            throw new RuntimeException("could not stop missing activity:" + activityDef);
        }
        scenariologger.debug("STOP " + activityDef.getAlias());
        activityExecutor.stopActivity();
    }

    /**
     * <p>Stop an activity, given an activity def map. The only part of the map that is important is the
     * alias parameter. This method retains the map signature to provide convenience for scripting.</p>
     *
     * @param activityDefMap A map, containing at least the alias parameter
     */
    public synchronized void stop(Map<String, String> activityDefMap) {
        ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        stop(ad);
    }

    /**
     * Stop an activity, given the name by which it is known already in the scenario. This causes the
     * activity to stop all threads, but keeps the thread objects handy for starting again. This can be useful
     * for certain testing scenarios in which you want to stop some workloads and start others based on other conditions.
     *
     * @param alias The name of the activity that is already known to the scenario
     */
    public synchronized void stop(String alias) {
        stop(aliasToDef(alias));
    }

    /**
     * Modify one of the parameters in a defined activity. Any observing activity components will be notified of the
     * changes made to activity parameters.
     *
     * @param alias The name of an activity that is already known to the scenario.
     * @param param The parameter name
     * @param value a new parameter value
     */
    public synchronized void modify(String alias, String param, String value) {
        if (param.equals("alias")) {
            throw new InvalidParameterException("It is not allowed to change the name of an existing activity.");
        }
        ActivityExecutor activityExecutor = getActivityExecutor(alias);
        ParameterMap params = activityExecutor.getActivityDef().getParams();
        scenariologger.debug("SET ("+alias+"/"+param + ")=(" + value + ")");
        params.set(param, value);
    }

    /**
     * Apply any parameter changes to a defined activity, or start a new one.
     * This method is syntactical sugar for scripting. Each of the parameters in the map
     * is checked against existing values, and per-field modifications
     * are applied one at a time, only if the values have changed.
     *
     * @param appliedParams Map of new values.
     */
    public synchronized void apply(Map<String, String> appliedParams) {
        String alias = appliedParams.get("alias");

        if (alias == null) {
            throw new BasicError("alias must be provided");
        }

        ActivityExecutor executor = activityExecutors.get(alias);

        if (executor == null) {
            logger.info("started scenario from apply:" + alias);
            start(appliedParams);
            return;
        }

        ParameterMap previousMap = executor.getActivityDef().getParams();

        for (String paramName : appliedParams.keySet()) {
            String appliedVal = appliedParams.get(paramName);
            Optional<String> prevVal = previousMap.getOptionalString(paramName);

            if (!prevVal.isPresent() || !prevVal.get().equals(appliedVal)) {
                logger.info("applying new value to activity '" + alias + "': '" + prevVal.get() + "' -> '" + appliedVal + "'");
                previousMap.set(paramName, appliedVal);
            }
        }
    }

    /**
     * Get the activity executor associated with the given alias. This should be used to find activitytypes
     * which are presumed to be already defined.
     *
     * @param activityAlias The activity alias for the extant activity.
     * @return the associated ActivityExecutor
     * @throws RuntimeException a runtime exception if the named activity is not found
     */
    private ActivityExecutor getActivityExecutor(String activityAlias) {
        Optional<ActivityExecutor> executor =
                Optional.ofNullable(activityExecutors.get(activityAlias));
        return executor.orElseThrow(
                () -> new RuntimeException("ActivityExecutor for alias " + activityAlias + " not found.")
        );

    }

    private ActivityExecutor getActivityExecutor(ActivityDef activityDef, boolean createIfMissing) {
        synchronized (activityExecutors) {
            ActivityExecutor executor = activityExecutors.get(activityDef.getAlias());

            if (executor == null && createIfMissing) {

                String activityTypeName = activityDef.getParams().getOptionalString("driver","type").orElse(null);
                List<String> knownTypes = ActivityType.FINDER.getAll().stream().map(ActivityType::getName).collect(Collectors.toList());

                // Infer the type from either alias or yaml if possible (exactly one matches)
                if (activityTypeName==null) {
                    List<String> matching = knownTypes.stream().filter(
                            n ->
                                    activityDef.getParams().getOptionalString("alias").orElse("").contains(n)
                                            || activityDef.getParams().getOptionalString("yaml", "workload").orElse("").contains(n)
                    ).collect(Collectors.toList());
                    if (matching.size()==1) {
                        activityTypeName=matching.get(0);
                        logger.info("param 'type' was inferred as '" + activityTypeName + "' since it was seen in yaml or alias parameter.");
                    }
                }

                if (activityTypeName==null) {
                    String errmsg = "You must provide a driver=<driver> parameter. Valid examples are:\n" +
                            knownTypes.stream().map(t -> " driver="+t+"\n").collect(Collectors.joining());
                    throw new BasicError(errmsg);
                }

                ActivityType<?> activityType = ActivityType.FINDER.getOrThrow(activityTypeName);
                executor = new ActivityExecutor(activityType.getAssembledActivity(activityDef, getActivityMap()));
                activityExecutors.put(activityDef.getAlias(), executor);
            }
            return executor;
        }
    }

    /**
     * Wait for a bit. This is not the best approach, and will be replace with a different system in the future.
     *
     * @param waitMillis time to wait, in milliseconds
     */
    public void waitMillis(long waitMillis) {
        scenariologger.debug("WAITMILLIS " + waitMillis);

        logger.trace("#> waitMillis(" + waitMillis + ")");
        long endTime = System.currentTimeMillis() + waitMillis;

        while (waitMillis > 0L) {
            try {
                Thread.sleep(waitMillis);
            } catch (InterruptedException spurrious) {
                waitMillis = endTime - System.currentTimeMillis();
                continue;
            }
            waitMillis = 0;
        }
    }

    /**
     * Return all the names of the activites that are known to this scenario.
     *
     * @return set of activity names
     */
    public Set<String> getAliases() {
        return activityExecutors.keySet();
    }

    /**
     * Return all the activity definitions that are known to this scenario.
     *
     * @return list of activity defs
     */
    public List<ActivityDef> getActivityDefs() {
        return activityExecutors.values().stream()
                .map(ActivityExecutor::getActivityDef)
                .collect(Collectors.toList());
    }

    /**
     * Get the named activity def, if it is known to this scenario.
     *
     * @param alias The name by which the activity is known to this scenario.
     * @return an ActivityDef instance
     * @throws RuntimeException if the alias is not known to the scenario
     */
    public ActivityDef getActivityDef(String alias) {
        return getActivityExecutor(alias).getActivityDef();
    }

    /**
     * Force the scenario to stop running. Stop all activity threads, and after waitTimeMillis, force stop
     * all activity threads. An activity will stop its threads cooperatively in this time as long as the
     * internal cycles complete before the timer expires.
     *
     * @param waitTimeMillis grace period during which an activity may cooperatively shut down
     */
    public void forceStopScenario(int waitTimeMillis) {
        logger.warn("Scenario force stopped.");
        activityExecutors.values().forEach(a -> a.forceStopExecutor(waitTimeMillis));
    }

    /**
     * Await completion of all running activities, but do not force shutdownActivity. This method is meant to provide
     * the blocking point for calling logic. It waits.
     *
     * @param waitTimeMillis The time to wait, usually set very high
     * @return true, if all activities completed before the timer expired, false otherwise
     */
    public boolean awaitCompletion(int waitTimeMillis) {
        boolean completed = false;
        for (ActivityExecutor executor : activityExecutors.values()) {
            if (!executor.awaitCompletion(waitTimeMillis)) {
                logger.debug("awaiting completion signaled FALSE");
                return false;
            }
        }
        logger.debug("All activities awaiting completion signaled TRUE");
        return true;
    }

    private ActivityDef aliasToDef(String alias) {
        if (alias.contains("=")) {
            return ActivityDef.parseActivityDef(alias);
        } else {
            return ActivityDef.parseActivityDef("alias=" + alias + ";");
        }
    }

    public boolean await(Map<String,String> activityDefMap) {
        return this.awaitActivity(activityDefMap);
    }
    public boolean awaitActivity(Map<String, String> activityDefMap) {
        ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        return awaitActivity(ad);
    }

    public boolean await(String alias) {
        return this.awaitActivity(alias);
    }
    public boolean awaitActivity(String alias) {
        ActivityDef toAwait = aliasToDef(alias);
        return awaitActivity(toAwait);
    }

    public boolean await(ActivityDef activityDef) {
        return this.awaitActivity(activityDef);
    }
    public boolean awaitActivity(ActivityDef activityDef) {
        ActivityExecutor activityExecutor = getActivityExecutor(activityDef, false);
        if (activityExecutor == null) {
            throw new RuntimeException("Could not await missing activity: " + activityDef);
        }
        scenariologger.debug("AWAIT/before alias=" + activityDef.getAlias());
        boolean finished = activityExecutor.awaitFinish(Integer.MAX_VALUE);
        scenariologger.debug("AWAIT/after  completed=" + finished);
        return finished;

    }

    /**
     * @return an unmodifyable String to executor map of all activities known to this scenario
     */
    public Map<String, ActivityExecutor> getActivityExecutorMap() {
        return Collections.unmodifiableMap(activityExecutors);
    }

    public void reportMetrics() {
        ActivityMetrics.reportTo(System.out);
    }

    private Map<String, Activity> getActivityMap() {
        Map<String, Activity> activityMap = new HashMap<String, Activity>();
        for (Map.Entry<String, ActivityExecutor> entry : activityExecutors.entrySet()) {
            activityMap.put(entry.getKey(), entry.getValue().getActivity());
        }
        return activityMap;
    }

    public Collection<ProgressMeter> getProgressMeters() {
        return this.activityExecutors.values().stream().map(e -> (ProgressMeter) e).collect(Collectors.toList());
    }
}
