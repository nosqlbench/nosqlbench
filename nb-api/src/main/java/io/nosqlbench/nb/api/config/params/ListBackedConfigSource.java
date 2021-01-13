package io.nosqlbench.nb.api.config.params;

import java.util.ArrayList;
import java.util.List;

public class ListBackedConfigSource implements ConfigSource {

    @Override
    public boolean canRead(Object source) {
        return (source instanceof List);
    }

    @Override
    public List<ElementData> getAll(Object source) {
        List<ElementData> data = new ArrayList<>();
        for (Object o : (List) source) {
            data.add(DataSources.element(o));
        }
        return data;
    }
}
