/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.core;

import io.nosqlbench.engine.core.script.Scenario;
import io.nosqlbench.engine.core.script.ScenariosExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScenariosResults {

    private static final Logger logger = LoggerFactory.getLogger(ScenariosResults.class);

    private String scenariosExecutorName;
    private Map<Scenario,ScenarioResult> scenarioResultMap = new LinkedHashMap<>();


    public ScenariosResults(ScenariosExecutor scenariosExecutor) {
        this.scenariosExecutorName =scenariosExecutor.getName();
    }

    public ScenariosResults(ScenariosExecutor scenariosExecutor, Map<Scenario, ScenarioResult> map) {
        this.scenariosExecutorName = scenariosExecutor.getName();
        scenarioResultMap.putAll(map);
    }


//    public void reportSummaryTo(PrintStream out) {
//        for (Map.Entry<Scenario, Result> entry : this.scenarioResultMap.entrySet()) {
//            Scenario scenario = entry.getKey();
//            Result oresult = entry.getValue();
//
//            out.println("results for scenario: " + scenario);
//
//            if (oresult!=null) {
//                oresult.reportTo(out);
//            } else {
//                out.println(": incomplete (missing result)");
//            }
//        }
//    }

    public ScenarioResult getOne() {
        if (this.scenarioResultMap.size()!=1) {
            throw new RuntimeException("getOne found " + this.scenarioResultMap.size() + " results instead of 1.");
        }
        return scenarioResultMap.values().stream().findFirst().orElseThrow(
                () -> new RuntimeException("Missing result."));
    }

    public void reportToLog() {
        for (Map.Entry<Scenario, ScenarioResult> entry : this.scenarioResultMap.entrySet()) {
            Scenario scenario = entry.getKey();
            ScenarioResult oresult = entry.getValue();

            logger.info("results for scenario: " + scenario);

            if (oresult!=null) {
                oresult.reportToLog();
            } else {
                logger.error(scenario.getName() + ": incomplete (missing result)");
            }
        }
    }

    public boolean hasError() {
        return this.scenarioResultMap.values().stream()
                .anyMatch(r -> r.getException().isPresent());
    }
}
