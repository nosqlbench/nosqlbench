package io.nosqlbench.nb.api.config.params;

import java.util.List;
import java.util.Map;

public class MapBackedConfigSource implements ConfigSource {

    @Override
    public boolean canRead(Object source) {
        return (source instanceof Map);
    }

    @Override
    public List<ElementData> getAll(Object source) {
        return List.of(new MapBackedElement((Map) source));
    }
}
