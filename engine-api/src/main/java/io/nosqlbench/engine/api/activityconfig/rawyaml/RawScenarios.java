package io.nosqlbench.engine.api.activityconfig.rawyaml;

import java.util.*;

public class RawScenarios extends LinkedHashMap<String, LinkedList<String>> {

    public List<String> getScenarioNames() {
        return new LinkedList<>(this.keySet());
    }

    public List<String> getNamedScenario(String scenarioName) {
        Object v = this.get(scenarioName);
        if (v==null) { return null; }

        // Yes this looks strange. Yes it will work. SnakeYaml and generics are a bad combo.
        if (v instanceof List) {
            return (List<String>) v;
        } else if (v instanceof CharSequence) {
            return List.of(v.toString());
        } else if (v instanceof Map) {
            Object[] o = ((Map) v).values().toArray();
            ArrayList<String> strings = new ArrayList<>(o.length);
            for (Object o1 : o) {
                strings.add(o1.toString());
            }
            return strings;
        } else {
            throw new RuntimeException("Unknown type while access raw named scenarios data: " + v.getClass().getCanonicalName());
        }
    }
}
