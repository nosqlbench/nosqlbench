package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.*;

public class RawScenarios extends LinkedHashMap<String, LinkedList<String>> {

    public static String STEPNAME = "%03d";

    public List<String> getScenarioNames() {
        return new LinkedList<>(this.keySet());
    }

    public Map<String,String> getNamedScenario(String scenarioName) {

        Object v = this.get(scenarioName);

        if (v==null) { return null; }

        // Yes this looks strange. Yes it will work. SnakeYaml and generics are a bad combo.
        if (v instanceof List) {
            List<String> list = (List<String>) v;
            Map<String,String> map = new LinkedHashMap<>();
            for (int i = 0; i < list.size(); i++) {
                map.put(String.format(STEPNAME,i),list.get(i));
            }
            return map;
        } else if (v instanceof CharSequence) {
            return Map.of(String.format(STEPNAME,1),v.toString());
        } else if (v instanceof Map) {
            return ((Map)v);
        } else {
            throw new RuntimeException("Unknown type while access raw named scenarios data: " + v.getClass().getCanonicalName());
        }
    }
}
