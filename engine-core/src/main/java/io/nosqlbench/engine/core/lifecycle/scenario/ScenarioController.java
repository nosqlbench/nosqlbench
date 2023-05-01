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
package io.nosqlbench.engine.core.lifecycle.scenario;

import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
import io.nosqlbench.api.config.NBLabeledElement;
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
public class ScenarioController implements NBLabeledElement {

    private static final Logger logger = LogManager.getLogger(ScenarioController.class);
    private static final Logger scenariologger = LogManager.getLogger("SCENARIO");

    private final ActivityLoader activityLoader;

    private final Map<String, ActivityRuntimeInfo> activityInfoMap = new ConcurrentHashMap<>();
    private final Scenario scenario;

    private final ExecutorService activitiesExecutor;

    public ScenarioController(final Scenario scenario) {
        this.scenario = scenario;
        activityLoader = new ActivityLoader(scenario);

        final ActivitiesExceptionHandler exceptionHandler = new ActivitiesExceptionHandler(this);
        final IndexedThreadFactory indexedThreadFactory = new IndexedThreadFactory("ACTIVITY", exceptionHandler);
        activitiesExecutor = Executors.newCachedThreadPool(indexedThreadFactory);
    }

    /**
     * Start an activity, given the activity definition for it. The activity will be known in the scenario
     * by the alias parameter.
     *
     * @param activityDef string in alias=value1;driver=value2;... format
     */
    public synchronized void start(final ActivityDef activityDef) {
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(this.scenario.getScenarioName())
            .now()
            .layer(Layer.Activity)
            .label("alias", activityDef.getAlias())
            .detail("command", "start")
            .detail("params", activityDef.toString())
            .build());

        this.doStartActivity(activityDef);
    }


    private synchronized ActivityRuntimeInfo doStartActivity(final ActivityDef activityDef) {
        if (!activityInfoMap.containsKey(activityDef.getAlias())) {
            final Activity activity = activityLoader.loadActivity(activityDef, this);
            final ActivityExecutor executor = new ActivityExecutor(activity, scenario.getScenarioName());
            final Future<ExecutionResult> startedActivity = this.activitiesExecutor.submit(executor);
            final ActivityRuntimeInfo activityRuntimeInfo = new ActivityRuntimeInfo(activity, startedActivity, executor);
            activityInfoMap.put(activity.getAlias(), activityRuntimeInfo);
        }
        return activityInfoMap.get(activityDef.getAlias());
    }

    /**
     * Start an activity, given a map which holds the activity definition for it. The activity will be known in
     * the scenario by the alias parameter.
     *
     * @param activityDefMap A map containing the activity definition
     */
    public synchronized void start(final Map<String, String> activityDefMap) {
        final ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        this.start(ad);
    }

    /**
     * Start an activity, given the name by which it is known already in the scenario. This is useful if you have
     * stopped an activity and want to start it again.
     *
     * @param alias the alias of an activity that is already known to the scenario
     */
    public synchronized void start(final String alias) {
        this.start(ActivityDef.parseActivityDef(alias));
    }

    public synchronized void run(final int timeout, final Map<String, String> activityDefMap) {
        final ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        this.run(ad, timeout);
    }

    /**
     * Synchronously run the defined activity with a timeout in seconds.
     *
     * @param timeoutMs   seconds to await completion of the activity.
     * @param activityDef A definition for an activity to run
     */
    public synchronized void run(final ActivityDef activityDef, final long timeoutMs) {
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(scenario.getScenarioName())
            .now()
            .layer(Layer.Activity)
            .label("alias", activityDef.getAlias())
            .detail("command", "run")
            .detail("params", activityDef.toString())
            .build());

        this.doStartActivity(activityDef);
        this.awaitActivity(activityDef, timeoutMs);
    }

    public synchronized void run(final int timeout, final String activityDefString) {
        final ActivityDef activityDef = ActivityDef.parseActivityDef(activityDefString);
        this.run(activityDef, timeout);
    }

    public synchronized void run(final Map<String, String> activityDefMap) {
        this.run(Integer.MAX_VALUE, activityDefMap);
    }

    public synchronized void run(final String activityDefString) {
        this.run(Integer.MAX_VALUE, activityDefString);
    }


    public synchronized void run(final ActivityDef activityDef) {
        this.run(activityDef, Long.MAX_VALUE);
    }


    public boolean isRunningActivity(final String alias) {
        return this.isRunningActivity(this.aliasToDef(alias));
    }

    public boolean isRunningActivity(final ActivityDef activityDef) {
        final ActivityRuntimeInfo runtimeInfo = activityInfoMap.get(activityDef.getAlias());
        return null != runtimeInfo && runtimeInfo.isRunning();
    }

    public boolean isRunningActivity(final Map<String, String> activityDefMap) {
        final ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        return this.isRunningActivity(ad);
    }

    /**
     * <p>Stop an activity, given an activity def. The only part of the activity def that is important is the
     * alias parameter. This method retains the activity def signature to provide convenience for scripting.</p>
     * <p>For example, sc.stop("alias=foo")</p>
     *
     * @param activityDef An activity def, including at least the alias parameter.
     */
    public synchronized void stop(final ActivityDef activityDef) {
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(scenario.getScenarioName())
            .now()
            .layer(Layer.Activity)
            .label("alias", activityDef.getAlias())
            .detail("command", "stop")
            .detail("params", activityDef.toString())
            .build());

        final ActivityRuntimeInfo runtimeInfo = activityInfoMap.get(activityDef.getAlias());
        if (null == runtimeInfo) throw new RuntimeException("could not stop missing activity:" + activityDef);

        ScenarioController.scenariologger.debug("STOP {}", activityDef.getAlias());

        runtimeInfo.stopActivity();
    }

    /**
     * <p>Stop an activity, given an activity def map. The only part of the map that is important is the
     * alias parameter. This method retains the map signature to provide convenience for scripting.</p>
     *
     * @param activityDefMap A map, containing at least the alias parameter
     */
    public synchronized void stop(final Map<String, String> activityDefMap) {
        final ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        this.stop(ad);
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
    public synchronized void stop(final String spec) {
        ScenarioController.logger.debug("request->STOP '{}'", spec);
        final List<String> aliases = Arrays.asList(spec.split("[,; ]"));
        final List<String> matched = aliases.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(aspec -> this.getMatchingAliases(aspec).stream()).collect(Collectors.toList());
        for (final String alias : matched) {
            final ActivityDef adef = this.aliasToDef(alias);
            ScenarioController.scenariologger.debug("STOP {}", adef.getAlias());
            this.stop(adef);
        }
    }

    /**
     * <p>Force stopping an activity, given an activity def. The only part of the activity def that is important is the
     * alias parameter. This method retains the activity def signature to provide convenience for scripting.</p>
     * <p>For example, sc.forceStop("alias=foo")</p>
     *
     * @param activityDef An activity def, including at least the alias parameter.
     */
    public synchronized void forceStop(final ActivityDef activityDef) {
        Annotators.recordAnnotation(Annotation.newBuilder()
            .session(scenario.getScenarioName())
            .now()
            .layer(Layer.Activity)
            .label("alias", activityDef.getAlias())
            .detail("command", "forceStop")
            .detail("params", activityDef.toString())
            .build());

        final ActivityRuntimeInfo runtimeInfo = activityInfoMap.get(activityDef.getAlias());
        if (null == runtimeInfo) throw new RuntimeException("could not force stop missing activity:" + activityDef);

        ScenarioController.scenariologger.debug("FORCE STOP {}", activityDef.getAlias());

        runtimeInfo.forceStopActivity();
    }

    /**
     * <p>Stop an activity, given an activity def map. The only part of the map that is important is the
     * alias parameter. This method retains the map signature to provide convenience for scripting.</p>
     *
     * @param activityDefMap A map, containing at least the alias parameter
     */
    public synchronized void forceStop(final Map<String, String> activityDefMap) {
        final ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        this.forceStop(ad);
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
    public synchronized void forceStop(final String spec) {
        ScenarioController.logger.debug("request->STOP '{}'", spec);
        final List<String> aliases = Arrays.asList(spec.split("[,; ]"));
        final List<String> matched = aliases.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(aspec -> this.getMatchingAliases(aspec).stream()).collect(Collectors.toList());
        for (final String alias : matched) {
            final ActivityDef adef = this.aliasToDef(alias);
            ScenarioController.scenariologger.debug("STOP {}", adef.getAlias());
            this.forceStop(adef);
        }
    }


    private List<String> getMatchingAliases(final String pattern) {
        final Pattern matcher;
        // If the pattern is an alphanumeric name, the require it to match as a fully-qualified literal
        // It is not, so the user is wanting to do a flexible match
        if (pattern.matches("[a-zA-Z_][a-zA-Z0-9_.]*")) matcher = Pattern.compile('^' + pattern + '$');
        else matcher = Pattern.compile(pattern);

        final List<String> matching = this.activityInfoMap.keySet().stream()
            .filter(a -> Pattern.matches(pattern, a))
            .peek(p -> ScenarioController.logger.debug("MATCH {} -> {}", pattern, p))
            .collect(Collectors.toList());
        return matching;
    }

    /**
     * Wait for a bit. This is not the best approach, and will be replaced with a different system in the future.
     *
     * @param waitMillis time to wait, in milliseconds
     */
    public void waitMillis(long waitMillis) {
        ScenarioController.scenariologger.debug("WAITMILLIS {}", waitMillis);

        ScenarioController.logger.trace("#> waitMillis({})", waitMillis);
        final long endTime = System.currentTimeMillis() + waitMillis;

        while (0L < waitMillis) {
            try {
                Thread.sleep(waitMillis);
            } catch (final InterruptedException spurrious) {
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
        return this.activityInfoMap.keySet();
    }

    /**
     * Force the scenario to stop running. Stop all activity threads, and after waitTimeMillis, force stop
     * all activity threads. An activity will stop its threads cooperatively in this time as long as the
     * internal cycles complete before the timer expires.
     *
     * @param waitTimeMillis grace period during which an activity may cooperatively shut down
     */
    public synchronized void forceStopScenario(final int waitTimeMillis, final boolean rethrow) {
        ScenarioController.logger.debug("force stopping scenario {}", scenario.getScenarioName());
        this.activityInfoMap.values().forEach(a -> a.getActivityExecutor().forceStopActivity(10000));
        ScenarioController.logger.debug("Scenario force stopped.");
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
    public boolean awaitCompletion(final long waitTimeMillis) {
        ScenarioController.logger.debug("awaiting completion");
        boolean completed = true;
        for (final ActivityRuntimeInfo activityRuntimeInfo : activityInfoMap.values()) {
            final ExecutionResult activityResult = activityRuntimeInfo.awaitResult(waitTimeMillis);
            if (null == activityResult) {
                ScenarioController.logger.error("Unable to retrieve activity result for {}", activityRuntimeInfo.getActivity().getAlias());
                completed = false;
            } else if (null != activityResult.getException()) {
                if (activityResult.getException() instanceof RuntimeException e) throw e;
                throw new RuntimeException(activityResult.getException());
            }
        }
        return completed;
    }

    private ActivityDef aliasToDef(final String alias) {
        if (alias.contains("=")) return ActivityDef.parseActivityDef(alias);
        return ActivityDef.parseActivityDef("alias=" + alias + ';');
    }

    public void await(final Map<String, String> activityDefMap) {
        awaitActivity(activityDefMap);
    }

    public boolean awaitActivity(final Map<String, String> activityDefMap) {
        final ActivityDef ad = new ActivityDef(new ParameterMap(activityDefMap));
        return this.awaitActivity(ad, Long.MAX_VALUE);
    }

    public boolean await(final String alias) {
        return awaitActivity(alias, Long.MAX_VALUE);
    }

    public boolean awaitActivity(final String alias, final long timeoutMs) {
        final ActivityDef toAwait = this.aliasToDef(alias);
        return this.awaitActivity(toAwait, Long.MAX_VALUE);
    }

    public void await(final ActivityDef activityDef, final long timeoutMs) {
        awaitActivity(activityDef, timeoutMs);
    }

    public boolean awaitActivity(final ActivityDef activityDef, final long timeoutMs) {
        final ActivityRuntimeInfo ari = activityInfoMap.get(activityDef.getAlias());
        if (null == ari) throw new RuntimeException("Could not await missing activity: " + activityDef.getAlias());
        ScenarioController.scenariologger.debug("AWAIT/before alias={}", activityDef.getAlias());
        ExecutionResult result = null;
        Future<ExecutionResult> future=null;
        try {
            future = ari.getFuture();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        try {
            result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } catch (final TimeoutException e) {
            throw new RuntimeException(e);
        }
        return null != result;
    }

    /**
     * @return an unmodifyable String to executor map of all activities known to this scenario
     */
    public Map<String, ActivityRuntimeInfo> getActivityExecutorMap() {
        return Collections.unmodifiableMap(this.activityInfoMap);
    }

    public List<ActivityDef> getActivityDefs() {
        return this.activityInfoMap.values().stream().map(ari -> ari.getActivity().getActivityDef()).toList();
    }

    public void reportMetrics() {
        ActivityMetrics.reportTo(System.out);
    }

    public List<ProgressMeterDisplay> getProgressMeters() {
        final List<ProgressMeterDisplay> indicators = new ArrayList<>();
        for (final ActivityRuntimeInfo ae : this.activityInfoMap.values()) indicators.add(ae.getProgressMeter());
        indicators.sort(Comparator.comparing(ProgressMeterDisplay::getStartTime));
        return indicators;
    }

    public void notifyException(final Thread t, final Throwable e) {
        ScenarioController.logger.error("Uncaught exception in activity lifecycle thread:{}", e, e);
        this.scenario.notifyException(t,e);
        throw new RuntimeException(e);
    }

    public ActivityDef getActivityDef(final String alias) {
        return this.activityInfoMap.get(alias).getActivity().getActivityDef();
    }

    public void shutdown() {
        ScenarioController.logger.debug(() -> "Requesting ScenarioController shutdown.");
        activitiesExecutor.shutdown();
        try {
            if (!activitiesExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                ScenarioController.logger.info(() -> "Scenario is being forced to shutdown after waiting 5 seconds for graceful shutdown.");
                activitiesExecutor.shutdownNow();
                if (!activitiesExecutor.awaitTermination(5, TimeUnit.SECONDS))
                    throw new RuntimeException("Unable to shutdown activities executor");
            }
        } catch (final Exception e) {
            ScenarioController.logger.warn("There was an exception while trying to shutdown the ScenarioController:{}", e, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getLabels() {
        return scenario.getLabels();
    }
}
