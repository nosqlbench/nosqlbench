package io.nosqlbench.nb.api.config.params;

import java.util.Map;
import java.util.Set;

public class MapBackedElement implements ElementData {

    private final Map<String, ?> map;
    private final String elementName;

    public MapBackedElement(String elementName, Map<String, ?> map) {
        this.elementName = elementName;
        this.map = map;
    }

    @Override
    public Object get(String name) {
        return map.get(name);
    }

    @Override
    public Set<String> getKeys() {
        return map.keySet();
    }

    @Override
    public boolean containsKey(String name) {
        return map.containsKey(name);
    }

    @Override
    public String getGivenName() {
        return this.elementName;
    }

    @Override
    public String toString() {
        return this.getGivenName() + "(" + (this.extractElementName() != null ? this.extractElementName() : "null") + "):" + map.toString();
    }
}
