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

import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.IndexedThreadFactory;
import io.nosqlbench.engine.core.lifecycle.scenario.script.ScenarioExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ScenariosExecutor {

    private final Logger logger = LogManager.getLogger("SCENARIOS");
    private final LinkedHashMap<String, SubmittedScenario> submitted = new LinkedHashMap<>();

    private final ExecutorService executor;
    private final String name;
    private RuntimeException stoppingException;

    public ScenariosExecutor(String name) {
        this(name, 1);
    }

    public ScenariosExecutor(String name, int threads) {
        executor = new ThreadPoolExecutor(1, threads,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            new IndexedThreadFactory("scenarios", new ScenarioExceptionHandler(this)));
        this.name = name;
    }

    public synchronized void execute(Scenario scenario) {
        if (submitted.get(scenario.getScenarioName()) != null) {
            throw new BasicError("Scenario " + scenario.getScenarioName() + " is already defined. Remove it first to reuse the name.");
        }
        Future<ExecutionMetricsResult> future = executor.submit(scenario);
        SubmittedScenario s = new SubmittedScenario(scenario, future);
        submitted.put(s.getName(), s);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Shuts down all running scenarios and awaits all results.
     *
     * @return the final scenario-result map.
     */
    public ScenariosResults awaitAllResults() {
        return awaitAllResults(Long.MAX_VALUE / 2, 60000); // half max value, to avoid overflow
    }

    /**
     * Shuts down all running scenarios and awaits all results.
     *
     * @param timeout        how long to wait for the results to complete
     * @param updateInterval how frequently to log status while waiting
     * @return the final scenario-result map
     */
    public ScenariosResults awaitAllResults(long timeout, long updateInterval) {
        long waitFrom = System.currentTimeMillis();
        if (updateInterval > timeout) {
            throw new BasicError("timeout must be equal to or greater than updateInterval");
        }
        long timeoutAt = System.currentTimeMillis() + timeout;

        executor.shutdown();
        boolean isShutdown = false;

        while (!isShutdown && System.currentTimeMillis() < timeoutAt) {
            long waitedAt = System.currentTimeMillis();
            long updateAt = Math.min(timeoutAt, waitedAt + updateInterval);
            while (!isShutdown && System.currentTimeMillis() < timeoutAt) {
                while (!isShutdown && System.currentTimeMillis() < updateAt) {
                    try {
                        long timeRemaining = updateAt - System.currentTimeMillis();
                        isShutdown = executor.awaitTermination(timeRemaining, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignored) {
                    }
                }
                logger.trace(() -> "waited " + (System.currentTimeMillis()-waitFrom) + " millis for scenarios");
                updateAt = Math.min(timeoutAt, System.currentTimeMillis() + updateInterval);
            }

            logger.debug("scenarios executor shutdown after " + (System.currentTimeMillis() - waitedAt) + "ms.");
        }

        if (!isShutdown) {
            throw new RuntimeException("executor still runningScenarios after awaiting all results for " + timeout
                + "ms.  isTerminated:" + executor.isTerminated() + " isShutdown:" + executor.isShutdown());
        }
        Map<Scenario, ExecutionMetricsResult> scenarioResultMap = new LinkedHashMap<>();
        getAsyncResultStatus()
            .entrySet()
            .forEach(
                es -> scenarioResultMap.put(
                    es.getKey(),
                    es.getValue().orElse(null)
                )
            );
        return new ScenariosResults(this, scenarioResultMap);
    }

    /**
     * @return list of scenarios which have been submitted, in order
     */
    public List<String> getPendingScenarios() {
        return new ArrayList<>(
            submitted.values().stream()
                .map(SubmittedScenario::getName)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    /**
     * <p>Returns a map of all pending scenario names and optional results.
     * All submitted scenarios are included. Those which are still pending
     * are returned with an empty option.</p>
     *
     * <p>Results may be exceptional. If {@link ExecutionMetricsResult#getException()} is present,
     * then the result did not complete normally.</p>
     *
     * @return map of async results, with incomplete results as Optional.empty()
     */
    public Map<Scenario, Optional<ExecutionMetricsResult>> getAsyncResultStatus() {

        Map<Scenario, Optional<ExecutionMetricsResult>> optResults = new LinkedHashMap<>();

        for (SubmittedScenario submittedScenario : submitted.values()) {
            Future<ExecutionMetricsResult> resultFuture = submittedScenario.getResultFuture();

            Optional<ExecutionMetricsResult> oResult = Optional.empty();
            if (resultFuture.isDone()) {
                try {
                    oResult = Optional.of(resultFuture.get());
                } catch (Exception e) {
                    long now = System.currentTimeMillis();
                    logger.debug("creating exceptional scenario result from getAsyncResultStatus");
                    oResult = Optional.of(new ExecutionMetricsResult(now, now, "errored output", e));
                }
            }

            optResults.put(submittedScenario.getScenario(), oResult);

        }

        return optResults;
    }

    public Optional<Scenario> getPendingScenario(String scenarioName) {
        return Optional.ofNullable(submitted.get(scenarioName)).map(SubmittedScenario::getScenario);
    }

    /**
     * Get the result of a pending or completed scenario. If the scenario has run to
     * completion, then the Optional will be present. If the scenario threw an
     * exception, or there was an error accessing the future, then the result will
     * contain the exception. If the callable for the scenario was cancelled, then the
     * result will contain an exception stating such.
     * <p>
     * If the scenario is still pending, then the optional will be empty.
     *
     * @param scenarioName the scenario name of interest
     * @return an optional result
     */
    public Optional<Future<ExecutionMetricsResult>> getPendingResult(String scenarioName) {
        return Optional.ofNullable(submitted.get(scenarioName)).map(s -> s.resultFuture);
    }

    public synchronized void stopScenario(String scenarioName) {
        this.stopScenario(scenarioName, false);
    }

    public synchronized void stopScenario(String scenarioName, boolean rethrow) {
        logger.debug("#stopScenario(name=" + scenarioName + ", rethrow="+ rethrow+")");
        Optional<Scenario> pendingScenario = getPendingScenario(scenarioName);
        if (pendingScenario.isPresent()) {
            pendingScenario.get().getScenarioController().forceStopScenario(10000, true);
        } else {
            throw new RuntimeException("Unable to cancel scenario: " + scenarioName + ": not found");
        }
    }

    public synchronized void deleteScenario(String scenarioName) {
        stopScenario(scenarioName, false);

        Optional<Scenario> pendingScenario = getPendingScenario(scenarioName);
        if (pendingScenario.isPresent()) {
            submitted.remove(scenarioName);
            logger.info(() -> "cancelled scenario " + scenarioName);
        } else {
            throw new RuntimeException("Unable to cancel scenario: " + scenarioName + ": not found");
        }
    }

    public String getName() {
        return name;
    }

    public synchronized void shutdownNow() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    private static class SubmittedScenario {
        private final Scenario scenario;
        private final Future<ExecutionMetricsResult> resultFuture;

        SubmittedScenario(Scenario scenario, Future<ExecutionMetricsResult> resultFuture) {
            this.scenario = scenario;
            this.resultFuture = resultFuture;
        }

        public Scenario getScenario() {
            return scenario;
        }

        Future<ExecutionMetricsResult> getResultFuture() {
            return resultFuture;
        }

        public String getName() {
            return scenario.getScenarioName();
        }
    }

    public synchronized void notifyException(Thread t, Throwable e) {
        logger.debug(() -> "Scenario executor uncaught exception: " + e.getMessage());
        this.stoppingException = new RuntimeException("Error in scenario thread " + t.getName(), e);
    }


}
