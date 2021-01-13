package io.nosqlbench.nb.api.config.params;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ElementImpl implements Element {

    private final ElementData data;

    public ElementImpl(ElementData data) {
        this.data = data;
    }

    public String getElementName() {
        return get(ElementData.NAME, String.class).orElse(null);
    }

    public <T> Optional<T> get(String name, Class<? extends T> classOfT) {
        List<String> path = Arrays.asList(name.split("\\."));

        ElementData top = data;
        int idx = 0;
        String lookup = path.get(idx);

        while (idx + 1 < path.size()) {
            if (!top.containsKey(lookup)) {
                throw new RuntimeException("unable to find '" + lookup + "' in '" + String.join(".", path));
            }
            Object o = top.get(lookup);
            top = DataSources.element(o);
//            top = top.getChildElementData(lookup);
            idx++;
            lookup = path.get(idx);
        }

        if (top.containsKey(lookup)) {
            Object elem = top.get(lookup);
            T convertedValue = top.convert(elem, classOfT);
//            T typeCastedValue = classOfT.cast(elem);
            return Optional.of(convertedValue);
        } else {
            return Optional.empty();
        }

    }

    public <T> T getOr(String name, T defaultValue) {
        Class<T> cls = (Class<T>) defaultValue.getClass();
        return get(name, cls).orElse(defaultValue);
    }


}
