package io.nosqlbench.nb.api.config.params;

import io.nosqlbench.nb.api.config.ParamsParser;

import java.util.List;
import java.util.Map;

public class ParamsParserSource implements ConfigSource {

    @Override
    public boolean canRead(Object source) {
        return (source instanceof CharSequence && ParamsParser.hasValues(source.toString()));
    }

    @Override
    public List<ElementData> getAll(Object source) {
        Map<String, String> paramsMap = ParamsParser.parse(source.toString(), false);
        return List.of(new MapBackedElement(paramsMap));
    }
}
