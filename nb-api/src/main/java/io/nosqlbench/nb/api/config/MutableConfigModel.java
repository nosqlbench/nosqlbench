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
            Object testValue = convertValueTo(ofType.getSimpleName(), configkey, value, element.getType());
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

    private Object convertValueTo(String configName, String paramName, Object value, Class<?> type) {
        try {
            if (type.isAssignableFrom(value.getClass())) {
                return type.cast(value);
            } else if (Number.class.isAssignableFrom(type)
                    && Number.class.isAssignableFrom(value.getClass())) {
                Number number = (Number) value;
                if (type.equals(Float.class)) {
                    return number.floatValue();
                } else if (type.equals(Integer.class)) {
                    return number.intValue();
                } else if (type.equals(Double.class)) {
                    return number.doubleValue();
                } else if (type.equals(Long.class)) {
                    return number.longValue();
                } else if (type.equals(Byte.class)) {
                    return number.byteValue();
                } else if (type.equals(Short.class)) {
                    return number.shortValue();
                } else {
                    throw new RuntimeException("Number type " + type.getSimpleName() + " could " +
                            " not be converted from " + value.getClass().getSimpleName());
                }
            }

        } catch (Exception e) {
            throw e;
        }

        throw new RuntimeException(
                "While configuring " + paramName + " for " + configName + ", " +
                        "Unable to convert " + value.getClass() + " to " +
                        type.getCanonicalName()
        );
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
                cval = convertValueTo(ofType.getSimpleName(), k, cval, type);
                validConfig.put(name, cval);
            }
        });

        return new ConfigReader(this.asReadOnly(), validConfig);
    }
}
