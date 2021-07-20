package io.nosqlbench.nb.api.config.params;

import java.util.List;
import java.util.Map;

public class MapBackedConfigSource implements ConfigSource {

    private String name;

    @Override
    public boolean canRead(Object source) {
        return (source instanceof Map);
    }

    @Override
    public List<ElementData> getAll(String name, Object source) {
        this.name = name;
        return List.of(new MapBackedElement(name, (Map) source));
    }

    @Override
    public String getName() {
        return name;
    }
}
