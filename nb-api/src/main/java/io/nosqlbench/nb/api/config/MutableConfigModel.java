package io.nosqlbench.nb.api.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutableConfigModel implements ConfigModel {

    private final List<ConfigModel.Element> elements = new ArrayList<>();

    public MutableConfigModel() {}

    public MutableConfigModel add(String name, Class<?> clazz) {
        add(new ConfigModel.Element(name, clazz));
        return this;
    }

    private void add(ConfigModel.Element element) {
        this.elements.add(element);
    }

    public ConfigModel asReadOnly() {
        return this;
    }

    @Override
    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }
}
