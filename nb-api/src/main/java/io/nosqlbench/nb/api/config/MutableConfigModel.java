package io.nosqlbench.nb.api.config;

import java.util.*;

public class MutableConfigModel implements ConfigModel {

    private final LinkedHashMap<String, ConfigElement> elements = new LinkedHashMap<>();
    private final Class<?> ofType;

    public MutableConfigModel(Class<?> ofType) {
        this.ofType = ofType;
    }

    public MutableConfigModel(Object ofObject) {
        this.ofType = ofObject.getClass();
    }

    public MutableConfigModel optional(String name, Class<?> clazz) {
        add(new ConfigElement(name, clazz, "", false, null));
        return this;
    }

    public MutableConfigModel optional(String name, Class<?> clazz, String description) {
        add(new ConfigElement(name, clazz, description, false, null));
        return this;
    }

    public MutableConfigModel required(String name, Class<?> clazz, String description) {
        add(new ConfigElement(name, clazz, description, true, null));
        return this;
    }

    public MutableConfigModel required(String name, Class<?> clazz) {
        add(new ConfigElement(name, clazz, "", true, null));
        return this;
    }

    public MutableConfigModel defaultto(String name, Object defaultValue) {
        add(new ConfigElement(name, defaultValue.getClass(), "", true, defaultValue));
        return this;
    }

    public MutableConfigModel defaultto(String name, Object defaultValue, String description) {
        add(new ConfigElement(name, defaultValue.getClass(), description, true, defaultValue));
        return this;
    }

    private void add(ConfigElement element) {
        this.elements.put(element.name, element);
    }

    public ConfigModel asReadOnly() {
        return this;
    }

    @Override
    public Map<String, ConfigElement> getElements() {
        return Collections.unmodifiableMap(elements);
    }

    @Override
    public Class<?> getOf() {
        return ofType;
    }

    @Override
    public void assertValidConfig(Map<String, ?> config) {
        for (String configkey : config.keySet()) {
            ConfigElement element = this.elements.get(configkey);
            if (element == null) {
                throw new RuntimeException(
                        "Unknown config parameter in config model '" + configkey + "'\n" +
                                "while configuring a " + getOf().getSimpleName());
            }
            Object value = config.get(configkey);
            if (!element.getType().isAssignableFrom(value.getClass())) {
                throw new RuntimeException("Unable to assign provided configuration\n" +
                        "of type '" + value.getClass().getSimpleName() + " to config\n" +
                        "parameter of type '" + element.getType().getSimpleName() + "'\n" +
                        "while configuring a " + getOf().getSimpleName());
            }
        }
        for (ConfigElement element : elements.values()) {
            if (element.isRequired() && element.getDefaultValue() == null) {
                if (!config.containsKey(element.getName())) {
                    throw new RuntimeException("A required config element named '" + element.getName() +
                            "' and type '" + element.getType().getSimpleName() + "' was not found\n" +
                            "for configuring a " + getOf().getSimpleName());
                }
            }
        }
    }

    @Override
    public ConfigReader apply(Map<String, ?> config) {
        assertValidConfig(config);
        LinkedHashMap<String, Object> validConfig = new LinkedHashMap<>();

        elements.forEach((k, v) -> {
            String name = v.getName();
            Class<?> type = v.getType();
            Object cval = config.get(name);
            if (cval == null && v.isRequired()) {
                cval = v.getDefaultValue();
            }
            if (cval != null) {
                if (type.isAssignableFrom(cval.getClass())) {
                    validConfig.put(name, cval);
                } else {
                    throw new RuntimeException("Unable to assign a " + cval.getClass().getSimpleName() +
                            " to a " + type.getSimpleName());
                }
            }
        });

        return new ConfigReader(this.asReadOnly(), validConfig);
    }
}
