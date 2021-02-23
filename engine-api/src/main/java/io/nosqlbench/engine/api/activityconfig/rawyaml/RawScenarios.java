package io.nosqlbench.engine.api.activityconfig.rawyaml;

import io.nosqlbench.nb.api.errors.BasicError;

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
