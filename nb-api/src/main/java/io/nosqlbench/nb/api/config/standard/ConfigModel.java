package io.nosqlbench.nb.api.config.standard;

import io.nosqlbench.nb.api.errors.BasicError;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigModel implements NBConfigModel {

    private final LinkedHashMap<String, Param<?>> elements = new LinkedHashMap<>();
    private Param<?> lastAdded = null;
    private final Class<?> ofType;

    private ConfigModel(Class<?> ofType, Param<?>... params) {
        this.ofType = ofType;
        for (Param<?> param : params) {
            this.elements.put(param.getName(), param);
        }
    }

    public static ConfigModel of(Class<?> ofType, Param<?>... params) {
        return new ConfigModel(ofType, params);
    }

    public ConfigModel optional(String name, Class<?> clazz) {
        add(new Param<>(name, clazz, "", false, null));
        return this;
    }

    public ConfigModel optional(String name, Class<?> clazz, String description) {
        add(new Param<>(name, clazz, description, false, null));
        return this;
    }

    public ConfigModel required(String name, Class<?> clazz, String description) {
        add(new Param<>(name, clazz, description, true, null));
        return this;
    }

    public <T> ConfigModel add(Param<T> param) {
        this.elements.put(param.name, param);
        lastAdded = null;
        return this;
    }

    public ConfigModel required(String name, Class<?> clazz) {
        add(new Param<>(name, clazz, "", true, null));
        return this;
    }

    public ConfigModel defaults(String name, Object defaultValue) {
        add(new Param<>(name, defaultValue.getClass(), "", true, defaultValue));
        return this;
    }

    public ConfigModel defaults(String name, Object defaultValue, String description) {
        add(new Param<>(name, defaultValue.getClass(), description, true, defaultValue));
        return this;
    }

    public ConfigModel describedAs(String descriptionOfLastElement) {
        lastAdded.setDescription(descriptionOfLastElement);
        return this;
    }


    public NBConfigModel asReadOnly() {
        return this;
    }

    @Override
    public Map<String, Param<?>> getElements() {
        return Collections.unmodifiableMap(elements);
    }

    @Override
    public Class<?> getOf() {
        return ofType;
    }

    @Override
    public void assertValidConfig(Map<String, ?> config) {
        for (String configkey : config.keySet()) {
            Param<?> element = this.elements.get(configkey);
            if (element == null) {
                StringBuilder paramhelp = new StringBuilder(
                    "Unknown config parameter in config model '" + configkey + "' " +
                        "while configuring " + getOf().getSimpleName()
                        + ", possible parameter names are " + this.elements.keySet() + "."
                );

                ConfigSuggestions.getForParam(this, configkey)
                    .ifPresent(suggestion -> paramhelp.append(" ").append(suggestion));

                throw new BasicError(paramhelp.toString());
            }
            Object value = config.get(configkey);
            Object testValue = convertValueTo(ofType.getSimpleName(), configkey, value, element.getType());
        }
        for (Param<?> element : elements.values()) {
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
    public NBConfiguration apply(Map<String, ?> config) {
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

        return new NBConfiguration(this.asReadOnly(), validConfig);
    }

    public ConfigModel validIfRegex(String s) {
        Pattern regex = Pattern.compile(s);
        lastAdded.setRegex(regex);
        return this;
    }
}
