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
package io.nosqlbench.engine.core.lifecycle.scenario.container;

import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityConfig;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponentErrorHandler;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.IndexedThreadFactory;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesExceptionHandler;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityExecutor;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityLoader;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityRuntimeInfo;
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
public class ContainerActivitiesController extends NBBaseComponent {

    private static final Logger logger = LogManager.getLogger(ContainerActivitiesController.class);
    private static final Logger scenariologger = LogManager.getLogger("SCENARIO");

    private final ActivityLoader activityLoader;
    private final Map<String, ActivityRuntimeInfo> activityInfoMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public ContainerActivitiesController(NBComponent parent) {
        super(parent);
        this.activityLoader = new ActivityLoader();
        ActivitiesExceptionHandler exceptionHandler = new ActivitiesExceptionHandler(this);
        IndexedThreadFactory indexedThreadFactory = new IndexedThreadFactory("ACTIVITY", exceptionHandler);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Start an activity, given the activity definition for it. The activity will be known in the scenario
     * by the alias parameter.
     *
     * @param ActivityConfig string in alias=value1;driver=value2;... format
     */
    public Activity start(ActivityConfig ActivityConfig) {
        ActivityRuntimeInfo ari = doStartActivity(ActivityConfig);
        return ari.getActivity();
    }


    private ActivityRuntimeInfo doStartActivity(ActivityConfig ActivityConfig) {
        if (!this.activityInfoMap.containsKey(ActivityConfig.getAlias())) {
            Activity activity = this.activityLoader.loadActivity(ActivityConfig, this);
            activity.initActivity();
            ActivityExecutor executor = new ActivityExecutor(activity);
            Future<ExecutionResult> startedActivity = executorService.submit(executor);
            ActivityRuntimeInfo activityRuntimeInfo = new ActivityRuntimeInfo(activity, startedActivity, executor);
            activityRuntimeInfo.getActivityExecutor().awaitMotorsRunningOrTerminalState();
            this.activityInfoMap.put(activity.getAlias(), activityRuntimeInfo);

        }
        return this.activityInfoMap.get(ActivityConfig.getAlias());
    }

    /**
     * Start an activity, given a map which holds the activity definition for it. The activity will be known in
     * the scenario by the alias parameter.
     *
     * @param activityParams A map containing the activity definition
     */
    public Activity start(Map<String, String> activityParams) {
        ActivityConfig ad = Activity.configFor(activityParams);
        Activity started = start(ad);
        awaitAllThreadsOnline(started,30000L);
        return started;
    }

    /**
     * Start an activity, given the name by which it is known already in the scenario. This is useful if you have
     * stopped an activity and want to start it again.
     *
     * @param alias the alias of an activity that is already known to the scenario
     */
    public Activity start(String alias) {
        return start(Activity.configFor(Map.of("alias",alias)));
    }

    public synchronized void run(int timeout, Map<String, String> activityParams) {
        ActivityConfig ad = Activity.configFor(activityParams);
        run(ad, timeout);
    }

    /**
     * Synchronously run the defined activity with a timeout in seconds.
     *
     * @param timeoutMs   seconds to await completion of the activity.
     * @param ActivityConfig A definition for an activity to run
     */
    public synchronized void run(ActivityConfig ActivityConfig, long timeoutMs) {

        doStartActivity(ActivityConfig);
        awaitActivity(ActivityConfig, timeoutMs);
    }

    public synchronized void run(int timeout, String ActivityConfigString) {
        Map<String, String> stringStringMap = ParameterMap.parseParams(ActivityConfigString)
            .map(p -> p.getStringStringMap()).orElseThrow();
        ActivityConfig activityConfig = Activity.configFor(stringStringMap);
        run(activityConfig, timeout);
    }

    public synchronized void run(Map<String, String> ActivityConfigMap) {
        run(Integer.MAX_VALUE, ActivityConfigMap);
    }

    public synchronized void run(String ActivityConfigString) {
        run(Integer.MAX_VALUE, ActivityConfigString);
    }


    public synchronized void run(ActivityConfig ActivityConfig) {
        run(ActivityConfig, Long.MAX_VALUE);
    }


    public boolean isRunningActivity(String alias) {
        return isRunningActivity(aliasToDef(alias));
    }

    public boolean isRunningActivity(ActivityConfig ActivityConfig) {
        ActivityRuntimeInfo runtimeInfo = this.activityInfoMap.get(ActivityConfig.getAlias());
        return (null != runtimeInfo) && runtimeInfo.isRunning();
    }

    public boolean isRunningActivity(Map<String, String> activityParams) {
        ActivityConfig ad = Activity.configFor(activityParams);
        return isRunningActivity(ad);
    }

    /**
     * <p>Stop an activity, given an activity def. The only part of the activity def that is important is the
     * alias parameter. This method retains the activity def signature to provide convenience for scripting.</p>
     * <p>For example, sc.stop("alias=foo")</p>
     *
     * @param ActivityConfig An activity def, including at least the alias parameter.
     */
    public synchronized void stop(ActivityConfig ActivityConfig) {

        ActivityRuntimeInfo runtimeInfo = this.activityInfoMap.get(ActivityConfig.getAlias());
        if (null == runtimeInfo) {
            throw new RuntimeException("could not stop missing activity:" + ActivityConfig);
        }

        scenariologger.debug("STOP {}", ActivityConfig.getAlias());
        runtimeInfo.stopActivity();
    }

    public boolean awaitAllThreadsOnline(ActivityConfig ActivityConfig, long timeoutMs) {
        ActivityRuntimeInfo runtimeInfo = this.activityInfoMap.get(ActivityConfig.getAlias());
        if (null == runtimeInfo) {
            throw new RuntimeException("could not stop missing activity:" + ActivityConfig);
        }

        scenariologger.debug("STOP {}", ActivityConfig.getAlias());
        return runtimeInfo.awaitAllThreadsOnline(timeoutMs);
    }

    public synchronized void stop(Activity activity) {
        stop(activity.getConfig());
    }


    public boolean awaitAllThreadsOnline(Activity activity, long timeoutMs) {
        return awaitAllThreadsOnline(activity.getConfig(), timeoutMs);
    }



    /**
     * <p>Stop an activity, given an activity def map. The only part of the map that is important is the
     * alias parameter. This method retains the map signature to provide convenience for scripting.</p>
     *
     * @param paramsMap A map, containing at least the alias parameter
     */
    public synchronized void stop(Map<String, String> paramsMap) {
        ActivityConfig ad = Activity.configFor(paramsMap);
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
        logger.debug("request->STOP '{}'", spec);
        List<String> aliases = Arrays.asList(spec.split("[,; ]"));
        List<String> matched = aliases.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(aspec -> getMatchingAliases(aspec).stream()).collect(Collectors.toList());
        for (String alias : matched) {
            ActivityConfig adef = aliasToDef(alias);
            scenariologger.debug("STOP {}", adef.getAlias());
            stop(adef);
        }
    }

    /**
     * <p>Force stopping an activity, given an activity def. The only part of the activity def that is important is the
     * alias parameter. This method retains the activity def signature to provide convenience for scripting.</p>
     * <p>For example, sc.forceStop("alias=foo")</p>
     *
     * @param ActivityConfig An activity def, including at least the alias parameter.
     */
    public synchronized void forceStop(ActivityConfig ActivityConfig) {

        ActivityRuntimeInfo runtimeInfo = this.activityInfoMap.get(ActivityConfig.getAlias());

        if (null == runtimeInfo) {
            throw new RuntimeException("could not force stop missing activity:" + ActivityConfig);
        }
        scenariologger.debug("FORCE STOP {}", ActivityConfig.getAlias());

        runtimeInfo.forceStopActivity();
    }

    /**
     * <p>Stop an activity, given an activity def map. The only part of the map that is important is the
     * alias parameter. This method retains the map signature to provide convenience for scripting.</p>
     *
     * @param activityParams A map, containing at least the alias parameter
     */
    public synchronized void forceStop(Map<String, String> activityParams) {
        ActivityConfig ad = Activity.configFor(activityParams);
        forceStop(ad);
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
    public synchronized void forceStop(String spec) {
        logger.debug("request->STOP '{}'", spec);
        List<String> aliases = Arrays.asList(spec.split("[,; ]"));
        List<String> matched = aliases.stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .flatMap(aspec -> getMatchingAliases(aspec).stream()).collect(Collectors.toList());
        for (String alias : matched) {
            ActivityConfig adef = aliasToDef(alias);
            scenariologger.debug("STOP {}", adef.getAlias());
            forceStop(adef);
        }
    }


    private List<String> getMatchingAliases(String pattern) {
        Pattern matcher;
        // If the pattern is an alphanumeric name, the require it to match as a fully-qualified literal
        // It is not, so the user is wanting to do a flexible match
        if (pattern.matches("[a-zA-Z_][a-zA-Z0-9_.]*")) {
            matcher = Pattern.compile('^' + pattern + '$');
        } else {
            matcher = Pattern.compile(pattern);
        }

        List<String> matching = activityInfoMap.keySet().stream()
            .filter(a -> Pattern.matches(pattern, a))
            .peek(p -> logger.debug("MATCH {} -> {}", pattern, p))
            .collect(Collectors.toList());
        return matching;
    }

    /**
     * Wait for a bit. This is not the best approach, and will be replaced with a different system in the future.
     *
     * @param waitMillis time to wait, in milliseconds
     */
    public void waitMillis(long waitMillis) {
        scenariologger.debug("WAITMILLIS {}", waitMillis);

        logger.trace("#> waitMillis({})", waitMillis);
        long endTime = System.currentTimeMillis() + waitMillis;

        while (0L < waitMillis) {
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
    public synchronized void forceStopActivities(int waitTimeMillis) {
        logger.debug("force stopping scenario {}", description());
        activityInfoMap.values().forEach(a -> a.getActivityExecutor().forceStopActivity(2000));
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
            if (null == activityResult) {
                logger.error("Unable to retrieve activity result for {}", activityRuntimeInfo.getActivity().getAlias());
                completed = false;
            } else if (null != activityResult.getException()) {
                if (activityResult.getException() instanceof RuntimeException e) {
                    throw e;
                }
                throw new RuntimeException(activityResult.getException());
            }
        }
        return completed;
    }

    private ActivityConfig aliasToDef(String alias) {
        String cfg = alias.contains("=") ? alias : "alias=" + alias;
        return Activity.configFor(ParameterMap.parseOrException(cfg).getStringStringMap());
    }

    public void await(Map<String, String> ActivityConfigMap) {
        this.awaitActivity(ActivityConfigMap);
    }

    public boolean awaitActivity(Map<String, String> activityParams) {
        ActivityConfig ad = Activity.configFor(activityParams);
        return awaitActivity(ad, Long.MAX_VALUE);
    }

    public boolean await(String alias) {
        return this.awaitActivity(alias, Long.MAX_VALUE);
    }

    public boolean awaitActivity(String alias, long timeoutMs) {
        ActivityConfig toAwait = aliasToDef(alias);
        return awaitActivity(toAwait, Long.MAX_VALUE);
    }

    public void await(ActivityConfig ActivityConfig, long timeoutMs) {
        this.awaitActivity(ActivityConfig, timeoutMs);
    }


    public boolean awaitActivity(ActivityConfig ActivityConfig, long timeoutMs) {
        ActivityRuntimeInfo ari = this.activityInfoMap.get(ActivityConfig.getAlias());
        if (null == ari) {
            throw new RuntimeException("Could not await missing activity: " + ActivityConfig.getAlias());
        }
        scenariologger.debug("AWAIT/before alias={}", ActivityConfig.getAlias());
        ExecutionResult result = null;
        Future<ExecutionResult> future=null;
        try {
            future = ari.getFuture();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        return null != result;
    }

    /**
     * @return an unmodifyable String to executor map of all activities known to this scenario
     */
    public Map<String, ActivityRuntimeInfo> getActivityExecutorMap() {
        return Collections.unmodifiableMap(activityInfoMap);
    }

    public List<ActivityConfig> getActivityConfigs() {
        return activityInfoMap.values().stream().map(ari -> ari.getActivity().getConfig()).toList();
    }

    public void reportMetrics() {
//        ActivityMetrics.reportTo(System.out);
    }

    public Optional<Activity> getSoloActivity() {
        if (this.getActivityExecutorMap().size()==1) {
            return Optional.of(activityInfoMap.values().iterator().next().getActivity());
        }
        return Optional.empty();
    }
    public Activity getActivity(String activityName) {
        return getOptionalActivity(activityName).orElseThrow(() -> new RuntimeException("Unable " +
            "to find required activity by name: '" + activityName + "'"));
    }
    public Optional<Activity> getOptionalActivity(String activityName) {
        return Optional.ofNullable(this.activityInfoMap.get(activityName)).map(ActivityRuntimeInfo::getActivity);
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
        logger.error("Uncaught exception in activity lifecycle thread:{}", e, e);
        if (getParent() instanceof NBComponentErrorHandler handler) {
            handler.notifyException(t,e);
        }
        throw new RuntimeException(e);
    }

    public ActivityConfig getActivityConfig(String alias) {
        return activityInfoMap.get(alias).getActivity().getConfig();
    }

    public void shutdown() {
        logger.debug(() -> "Requesting ScenarioController shutdown.");
        this.executorService.shutdownNow();
//        try {
//            if (!this.activitiesExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
//                logger.info(() -> "Scenario is being forced to shutdown after waiting 5 seconds for graceful shutdown.");
//                this.activitiesExecutor.shutdownNow();
//                if (!this.activitiesExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
//                    logger.warn("Unable to shutdown activities executor gracefully");
//                }
//            }
//        } catch (Exception e) {
//            logger.warn("There was an exception while trying to shutdown the ScenarioController:{}", e, e);
//            throw new RuntimeException(e);
//        }
    }

}
