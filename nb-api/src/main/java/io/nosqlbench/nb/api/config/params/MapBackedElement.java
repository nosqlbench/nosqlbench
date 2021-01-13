package io.nosqlbench.nb.api.config.params;

import java.util.Map;

public class MapBackedElement implements ElementData {

    private final Map map;

    public MapBackedElement(Map map) {
        this.map = map;
    }

    @Override
    public Object get(String name) {
        return map.get(name);
    }

    @Override
    public boolean containsKey(String name) {
        return map.containsKey(name);
    }
}
