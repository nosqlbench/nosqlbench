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

package io.nosqlbench.adapters.api.activityconfig.rawyaml;

import io.nosqlbench.api.errors.BasicError;

import java.util.*;

public class RawScenarios {

    public static String STEPNAME = "%03d";
    private final Map<String, Map<String, String>> scenarios = new LinkedHashMap<>();

    public List<String> getScenarioNames() {
        return new ArrayList<>(scenarios.keySet());
    }

    public void setPropertiesByReflection(Object scenariosObject) {
        scenarios.clear();

        Objects.requireNonNull(scenariosObject);
        if (scenariosObject instanceof Map) {
            Map<String, Object> rawNamedScenarios = (Map<String, Object>) scenariosObject;
            for (Map.Entry<String, Object> namedEntry : rawNamedScenarios.entrySet()) {
                String scenarioName = namedEntry.getKey();
                Object scenarioObj = namedEntry.getValue();
                if (scenarioObj == null) {
                    throw new BasicError("Unable to use a null value for scenario named " + scenarioName + " in yaml.");
                }
                if (scenarioObj instanceof CharSequence) {
                    scenarios.put(scenarioName, Map.of(String.format(STEPNAME, 1), scenarioObj.toString()));
                } else if (scenarioObj instanceof List) {
                    List<String> list = (List<String>) scenarioObj;
                    Map<String, String> scenarioMap = new LinkedHashMap<>();
                    for (int i = 0; i < list.size(); i++) {
                        scenarioMap.put(String.format(STEPNAME, i), list.get(i));
                    }
                    scenarios.put(scenarioName, scenarioMap);
                } else if (scenarioObj instanceof Map) {
                    scenarios.put(scenarioName, (Map<String,String>)scenarioObj);
                }
            }
        } else {
            throw new RuntimeException("Named scenarios must be a map at the top level, instead found '" + scenariosObject.getClass().getCanonicalName() + "'");
        }
    }

    public Map<String, String> getNamedScenario(String scenarioName) {
        return scenarios.get(scenarioName);
    }
}
