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

import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ScenariosResults {

    private static final Logger logger = LogManager.getLogger(ScenariosResults.class);
    private final String scenariosExecutorName;
    private final Map<Scenario, ExecutionMetricsResult> scenarioResultMap = new LinkedHashMap<>();


    public ScenariosResults(ScenariosExecutor scenariosExecutor) {
        this.scenariosExecutorName = scenariosExecutor.getName();
    }

    public ScenariosResults(ScenariosExecutor scenariosExecutor, Map<Scenario, ExecutionMetricsResult> map) {
        this.scenariosExecutorName = scenariosExecutor.getName();
        scenarioResultMap.putAll(map);
    }

    public String getExecutionSummary() {
        String sb = "executions: " + scenarioResultMap.size() + " scenarios, " +
            scenarioResultMap.values().stream().filter(r -> r.getException()==null).count() + " normal, " +
            scenarioResultMap.values().stream().filter(r -> r.getException()!=null).count() + " errored";
        return sb;
    }

    public ExecutionMetricsResult getOne() {
        if (this.scenarioResultMap.size() != 1) {
            throw new RuntimeException("getOne found " + this.scenarioResultMap.size() + " results instead of 1.");
        }
        return scenarioResultMap.values().stream().findFirst().orElseThrow(
                () -> new RuntimeException("Missing result."));
    }

    public void reportToLog() {
        for (Map.Entry<Scenario, ExecutionMetricsResult> entry : this.scenarioResultMap.entrySet()) {
            Scenario scenario = entry.getKey();
            ExecutionMetricsResult oresult = entry.getValue();

            logger.info(() -> "results for scenario: " + scenario);

            if (oresult != null) {
                oresult.reportElapsedMillisToLog();
            } else {
                logger.error(scenario.getScenarioName() + ": incomplete (missing result)");
            }

        }
    }

    public boolean hasError() {
        return this.scenarioResultMap.values().stream()
                .anyMatch(r -> r.getException()!=null);
    }

    public Optional<Exception> getAnyError() {
        return this.scenarioResultMap.values().stream()
            .map(ExecutionResult::getException).filter(Objects::nonNull).findFirst();
    }

    public int getSize() {
        return this.scenarioResultMap.size();
    }
}
