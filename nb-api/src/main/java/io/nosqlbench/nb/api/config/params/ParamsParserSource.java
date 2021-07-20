package io.nosqlbench.nb.api.config.params;

import java.util.List;
import java.util.Map;

public class ParamsParserSource implements ConfigSource {

    private String name;

    @Override
    public boolean canRead(Object source) {
        return (source instanceof CharSequence && ParamsParser.hasValues(source.toString()));
    }

    @Override
    public List<ElementData> getAll(String name,Object source) {
        this.name = name;
        Map<String, String> paramsMap = ParamsParser.parse(source.toString(), false);
        return List.of(new MapBackedElement(name, paramsMap));
    }

    @Override
    public String getName() {
        return name;
    }
}
