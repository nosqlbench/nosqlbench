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
package io.nosqlbench.engine.core.lifecycle.scenario;

import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.IndexedThreadFactory;
import io.nosqlbench.engine.core.lifecycle.activity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A ScenarioController provides a way to start Activities,
 * modify them while running, and forceStopMotors, pause or restart them.
 */
public class ScenarioController {

    private static final Logger logger = LogManager.getLogger(ScenarioController.class);
    private static final Logger scenariologger = LogManager.getLogger("SCENARIO");

    private final ActivityLoader activityLoader;

    private final Map<String, ActivityRuntimeInfo> activityInfoMap = new ConcurrentHashMap<>();
    private final Scenario scenario;

    private final ExecutorService activitiesExecutor;

    public ScenarioController(Scenario scenario) {
        this.scenario = scenario;
        this.activityLoader = new ActivityLoader(scenario);

        ActivitiesExceptionHandler exceptionHandler = new ActivitiesExceptionHandler(this);
        IndexedThreadFactory indexedThreadFactory = new IndexedThreadFactory("ACTIVITY", exceptionHandler);
        this.activitiesExecutor = Executors.newCachedThreadPool(indexedThreadFactory);
    }

    /**
     * Start an activity, given the activity definition for it. The activity will be known in the scenario
     * by the alias parameter.
     *
     * @param activityDef string in alias=value1;driver=value2;... format
     */
    public synchronized void start(ActivityDef activityDef) {
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(scenario.getScenarioName())
            .now()
            .layer(Layer.Activity)
            .label("alias", activityDef.getAlias())
            .detail("command", "start")
            .detail("params", activityDef.toString())
            .build());

        doStartActivity(activityDef);
    }


    private synchronized ActivityRuntimeInfo doStartActivity(ActivityDef activityDef) {
        if (!this.activityInfoMap.containsKey(activityDef.getAlias())) {
            Activity activity = this.activityLoader.loadActivity(activityDef);
            ActivityExecutor executor = new ActivityExecutor(activity, this.scenario.getScenarioName());
            Future<ExecutionResult> startedActivity = activitiesExecutor.submit(executor);
            ActivityRuntimeInfo activityRuntimeInfo = new ActivityRuntimeInfo(activity, startedActivity, executor);
            this.activityInfoMap.put(activity.getAlias(), activityRuntimeInfo);
            executor.startActivity();
            scenariologger.debug("STARTED " + activityDef.getAlias());
        }
        return this.activityInfoMap.get(activityDef.getAlias());
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
        run(ad, timeout);
    }

    /**
     * Synchronously run the defined activity with a timeout in seconds.
     *
     * @param timeoutMs   seconds to await completion of the activity.
     * @param activityDef A definition for an activity to run
     */
    public synchronized void run(ActivityDef activityDef, long timeoutMs) {
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(this.scenario.getScenarioName())
            .now()
            .layer(Layer.Activity)
            .label("alias", activityDef.getAlias())
            .detail("command", "run")
            .detail("params", activityDef.toString())
            .build());

        doStartActivity(activityDef);
        awaitActivity(activityDef, timeoutMs);
    }

    public synchronized void run(int timeout, String activityDefString) {
        ActivityDef activityDef = ActivityDef.parseActivityDef(activityDefString);
        run(activityDef, timeout);
    }

    public synchronized void run(Map<String, String> activityDefMap) {
        run(Integer.MAX_VALUE, activityDefMap);
    }

    public synchronized void run(String activityDefString) {
        run(Integer.MAX_VALUE, activityDefString);
    }


    public synchronized void run(ActivityDef activityDef) {
        run(activityDef, Long.MAX_VALUE);
    }


    public boolean isRunningActivity(String alias) {
        return isRunningActivity(aliasToDef(alias));
    }

    public boolean isRunningActivity(ActivityDef activityDef) {
        ActivityRuntimeInfo runtimeInfo = this.activityInfoMap.get(activityDef.getAlias());
        return (runtimeInfo != null && runtimeInfo.isRunning());
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
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(this.scenario.getScenarioName())
            .now()
            .layer(Layer.Activity)
            .label("alias", activityDef.getAlias())
            .detail("command", "stop")
            .detail("params", activityDef.toString())
            .build());

        ActivityRuntimeInfo runtimeInfo = this.activityInfoMap.get(activityDef.getAlias());
        if (runtimeInfo == null) {
            throw new RuntimeException("could not stop missing activity:" + activityDef);
        }

        scenariologger.debug("STOP " + activityDef.getAlias());

        runtimeInfo.stopActivity();
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
     * Alternately, you can provide one or more aliases in the same command, and all matching names will be stopped.
     *
     * @param spec The name of the activity that is already known to the scenario
     */
    public synchronized void stop(String spec) {
        logger.debug("request->STOP '" + spec + "'");
        List<String> aliases = Arrays.asList(spec.split("[,; ]"));
        List<String> matched = aliases.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(aspec -> getMatchingAliases(aspec).stream()).collect(Collectors.toList());
        for (String alias : matched) {
            ActivityDef adef = aliasToDef(alias);
            scenariologger.debug("STOP " + adef.getAlias());
            stop(adef);
        }
    }


    private List<String> getMatchingAliases(String pattern) {
        Pattern matcher;
        // If the pattern is an alphanumeric name, the require it to match as a fully-qualified literal
        if (pattern.matches("[a-zA-Z_][a-zA-Z0-9_.]*")) {
            matcher = Pattern.compile("^" + pattern + "$");
        } else { // It is not, so the user is wanting to do a flexible match
            matcher = Pattern.compile(pattern);
        }

        List<String> matching = activityInfoMap.keySet().stream()
            .filter(a -> Pattern.matches(pattern, a))
            .peek(p -> logger.debug("MATCH " + pattern + " -> " + p))
            .collect(Collectors.toList());
        return matching;
    }

    /**
     * Wait for a bit. This is not the best approach, and will be replaced with a different system in the future.
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
        return activityInfoMap.keySet();
    }

    /**
     * Force the scenario to stop running. Stop all activity threads, and after waitTimeMillis, force stop
     * all activity threads. An activity will stop its threads cooperatively in this time as long as the
     * internal cycles complete before the timer expires.
     *
     * @param waitTimeMillis grace period during which an activity may cooperatively shut down
     */
    public synchronized void forceStopScenario(int waitTimeMillis, boolean rethrow) {
        logger.debug("force stopping scenario " + this.scenario.getScenarioName());
        activityInfoMap.values().forEach(a -> a.getActivityExecutor().forceStopActivity(10000));
        logger.debug("Scenario force stopped.");
    }

//    public synchronized void stopAll() {
//        this.forceStopScenario(5000,false);
//    }

    /**
     * Await completion of all running activities, but do not force shutdownActivity. This method is meant to provide
     * the blocking point for calling logic. It waits. If there is an error which should propagate into the scenario,
     * then it should be thrown from this method.
     *
     * @param waitTimeMillis The time to wait, usually set very high
     * @return true, if all activities completed before the timer expired, false otherwise
     */
    public boolean awaitCompletion(long waitTimeMillis) {
        logger.debug("awaiting completion");
        boolean completed = true;
        for (ActivityRuntimeInfo activityRuntimeInfo : this.activityInfoMap.values()) {
            ExecutionResult activityResult = activityRuntimeInfo.awaitResult(waitTimeMillis);
            if (activityResult == null) {
                logger.error("Unable to retrieve activity result for " + activityRuntimeInfo.getActivity().getAlias());
                completed = false;
            } else {
                if (activityResult.getException()!=null) {
                    if (activityResult.getException() instanceof RuntimeException e) {
                        throw e;
                    } else {
                        throw new RuntimeException(activityResult.getException());
                    }
                }
            }
        }
        return completed;
    }

    private ActivityDef aliasToDef(String alias) {
        if (alias.contains("=")) {
            return ActivityDef.parseActivityDef(alias);
        } else {
            return ActivityDef.parseActivityDef("alias=" + alias + ";");
        }
    }

    public void await(Map<String, String> activityDefMap) {
        this.awaitActivity(activityDefMap);
    }

    public boolean awaitActivity(Map<String, String> activityDefMap) {
        ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        return awaitActivity(ad, Long.MAX_VALUE);
    }

    public boolean await(String alias) {
        return this.awaitActivity(alias, Long.MAX_VALUE);
    }

    public boolean awaitActivity(String alias, long timeoutMs) {
        ActivityDef toAwait = aliasToDef(alias);
        return awaitActivity(toAwait, Long.MAX_VALUE);
    }

    public void await(ActivityDef activityDef, long timeoutMs) {
        this.awaitActivity(activityDef, timeoutMs);
    }

    public boolean awaitActivity(ActivityDef activityDef, long timeoutMs) {
        ActivityRuntimeInfo ari = this.activityInfoMap.get(activityDef.getAlias());
        if (ari == null) {
            throw new RuntimeException("Could not await missing activity: " + activityDef.getAlias());
        }
        scenariologger.debug("AWAIT/before alias=" + activityDef.getAlias());
        ExecutionResult result = null;
        Future<ExecutionResult> future=null;
        try {
            future = ari.getFuture();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        return (result != null);
    }

    /**
     * @return an unmodifyable String to executor map of all activities known to this scenario
     */
    public Map<String, ActivityRuntimeInfo> getActivityExecutorMap() {
        return Collections.unmodifiableMap(activityInfoMap);
    }

    public List<ActivityDef> getActivityDefs() {
        return activityInfoMap.values().stream().map(ari -> ari.getActivity().getActivityDef()).toList();
    }

    public void reportMetrics() {
        ActivityMetrics.reportTo(System.out);
    }

    public List<ProgressMeterDisplay> getProgressMeters() {
        List<ProgressMeterDisplay> indicators = new ArrayList<>();
        for (ActivityRuntimeInfo ae : activityInfoMap.values()) {
            indicators.add(ae.getProgressMeter());
        }
        indicators.sort(Comparator.comparing(ProgressMeterDisplay::getStartTime));
        return indicators;
    }

    public void notifyException(Thread t, Throwable e) {
        logger.error("Uncaught exception in activity lifecycle thread:" + e, e);
        scenario.notifyException(t,e);
        throw new RuntimeException(e);
    }

    public ActivityDef getActivityDef(String alias) {
        return activityInfoMap.get(alias).getActivity().getActivityDef();
    }

    public void shutdown() {
        logger.debug(() -> "Requesting ScenarioController shutdown.");
        this.activitiesExecutor.shutdown();
        try {
            if (!this.activitiesExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.info(() -> "Scenario is being forced to shutdown after waiting 5 seconds for graceful shutdown.");
                this.activitiesExecutor.shutdownNow();
                if (!this.activitiesExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Unable to shutdown activities executor");
                }
            }
        } catch (Exception e) {
            logger.warn("There was an exception while trying to shutdown the ScenarioController:" + e,e);
            throw new RuntimeException(e);
        }
    }
}
