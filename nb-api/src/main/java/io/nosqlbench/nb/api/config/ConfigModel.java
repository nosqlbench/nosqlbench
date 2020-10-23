package io.nosqlbench.nb.api.config;

import java.util.List;

public interface ConfigModel {
    List<Element> getElements();

    class Element {
        public final String name;
        public final Class<?> type;

        public Element(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
    }
}
