package io.nosqlbench.nb.api.config.standard;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.BasicError;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class ConfigModel implements NBConfigModel {

    private final Map<String, Param<?>> paramsByName = new LinkedHashMap<>();
    private final List<Param<?>> params = new ArrayList<>();
    private Param<?> lastAdded = null;
    private final Class<?> ofType;

    private ConfigModel(Class<?> ofType, Param<?>... params) {
        this.ofType = ofType;
        for (Param<?> param : params) {
            add(param);
        }
    }

    public static ConfigModel of(Class<?> ofType, Param<?>... params) {
        return new ConfigModel(ofType, params);
    }
    public static ConfigModel of(Class<?> ofType) {
        return new ConfigModel(ofType);
    }

    public static NBConfiguration defacto(ActivityDef def) {
        ConfigModel configModel = new ConfigModel(Object.class);
        for (Map.Entry<String, Object> entry : def.getParams().entrySet()) {
            configModel.add(Param.defaultTo(entry.getKey(),entry.getValue().toString()));
        }
        return configModel.apply(def.getParams());
    }

    public <T> ConfigModel add(Param<T> param) {
        this.params.add(param);
        for (String name : param.getNames()) {
            paramsByName.put(name, param);
        }
        lastAdded = null;
        return this;
    }

    public NBConfigModel asReadOnly() {
        return this;
    }

    @Override
    public Map<String, Param<?>> getNamedParams() {
        return Collections.unmodifiableMap(paramsByName);
    }

    @Override
    public List<Param<?>> getParams() {
        return new ArrayList<>((this.params));
    }

    @Override
    public Class<?> getOf() {
        return ofType;
    }

    public static <T> T convertValueTo(String configName, String paramName, Object value, Class<T> type) {
        try {
            if (type.isAssignableFrom(value.getClass())) {
                return type.cast(value);

            } else if (Number.class.isAssignableFrom(value.getClass())) { // A numeric value, and do we have a compatible target type?
                Number number = (Number) value;
                // This series of double fake-outs is heinous, but it works to get around design
                // holes in Java generics while preserving some type inference for the caller.
                // If you are reading this code and you can find a better way, please change it!
                if (type.equals(Float.class) || type == float.class) {
                    return (T) (Float) number.floatValue();
                } else if (type.equals(Integer.class) || type == int.class) {
                    return (T) (Integer) number.intValue();
                } else if (type.equals(Double.class) || type == double.class) {
                    return (T) (Double) number.doubleValue();
                } else if (type.equals(Long.class) || type == long.class) {
                    return (T) (Long) number.longValue();
                } else if (type.equals(Byte.class) || type == byte.class) {
                    return (T) (Byte) number.byteValue();
                } else if (type.equals(Short.class) || type == short.class) {
                    return (T) (Short) number.shortValue();
                } else {
                    throw new RuntimeException("Number type " + type.getSimpleName() + " could " +
                        " not be converted from " + value.getClass().getSimpleName());
                }
            } else if (value instanceof CharSequence) { // A stringy type, and do we have a compatible target type?
                String string = ((CharSequence) value).toString();

                if (type == int.class || type == Integer.class) {
                    return (T) Integer.valueOf(string);
                } else if (type == char.class || type == Character.class && string.length() == 1) {
                    return (T) (Character) string.charAt(0);
                } else if (type == long.class || type == Long.class) {
                    return (T) Long.valueOf(string);
                } else if (type == float.class || type == Float.class) {
                    return (T) Float.valueOf(string);
                } else if (type == double.class || type == Double.class) {
                    return (T) Double.valueOf(string);
                } else if (type == BigDecimal.class) {
                    return (T) BigDecimal.valueOf(Double.parseDouble(string));
                } else {
                    throw new RuntimeException("CharSequence type " + type.getSimpleName() + " could " +
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
    public NBConfiguration extractConfig(Map<String, ?> sharedConfig) {
        LinkedHashMap<String, Object> extracted = new LinkedHashMap<>();
        for (String providedCfgField : sharedConfig.keySet()) {
            if (getNamedParams().containsKey(providedCfgField)) {
                extracted.put(providedCfgField, sharedConfig.get(providedCfgField));
            }
        }
        extracted.keySet().forEach(sharedConfig::remove);

        return new NBConfiguration(this, extracted);
    }

    @Override
    public NBConfiguration extractConfig(NBConfiguration cfg) {
        return extractConfig(cfg.getMap());
    }

    private void assertDistinctSynonyms(Map<String, ?> config) {
        List<String> names = new ArrayList<>();
        for (Param<?> param : getParams()) {
            names.clear();
            for (String s : param.getNames()) {
                if (config.containsKey(s)) {
                    names.add(s);
                }
            }
            if (names.size() > 1) {
                throw new NBConfigError("Multiple names for the same parameter were provided: " + names);
            }
        }
    }

    @Override
    public NBConfiguration apply(Map<String, ?> config) {
        assertValidConfig(config);
        LinkedHashMap<String, Object> validConfig = new LinkedHashMap<>();

        for (Param<?> param : params) {
            Class<?> type = param.getType();
            List<String> found = new ArrayList<>();
            String activename = null;
            Object cval = null;
            for (String name : param.getNames()) {
                if (config.containsKey(name)) {
                    cval = config.get(name);
                    activename = name;
                    break;
                }
            }
            if (activename==null) {
                activename = param.getNames().get(0);
            }
            if (cval == null && param.isRequired()) {
                activename = param.getNames().get(0);
                cval = param.getDefaultValue();  // We know this will be valid. It was validated, correct?
            }
            if (cval != null) {
                cval = convertValueTo(ofType.getSimpleName(), activename, cval, type);
                validConfig.put(activename, cval);
            }
        }
        return new NBConfiguration(this.asReadOnly(), validConfig);
    }

    @Override
    public void assertValidConfig(Map<String, ?> config) {
        assertRequiredFields(config);
        assertNoExtraneousFields(config);
        assertDistinctSynonyms(config);
    }

    @Override
    public Param<?> getParam(String... names) {
        for (String name : names) {
            if (this.getNamedParams().containsKey(name)) {
                return this.getNamedParams().get(name);
            }
        }
        return null;
    }

    public ConfigModel validIfRegex(String s) {
        Pattern regex = Pattern.compile(s);
        lastAdded.setRegex(regex);
        return this;
    }

    private void assertRequiredFields(Map<String, ?> config) {
        // For each known configuration model ...
        for (Param<?> param : params) {
            if (param.isRequired() && param.getDefaultValue() == null) {
                boolean provided = false;
                for (String name : param.getNames()) {
                    if (config.containsKey(name)) {
                        provided = true;
                        break;
                    }
                }
                if (!provided) {
                    throw new RuntimeException("A required config element named '" + param.getNames() +
                        "' and type '" + param.getType().getSimpleName() + "' was not found\n" +
                        "for configuring a " + getOf().getSimpleName());
                }

            }
        }
    }

    private void assertNoExtraneousFields(Map<String, ?> config) {
        // For each provided configuration element ...
        for (String configkey : config.keySet()) {
            Param<?> element = this.paramsByName.get(configkey);
            if (element == null) {
                StringBuilder paramhelp = new StringBuilder(
                    "Unknown config parameter '" + configkey + "' in config model while configuring " + getOf().getSimpleName()
                        + ", possible parameter names are " + this.paramsByName.keySet() + "."
                );

                ConfigSuggestions.getForParam(this, configkey)
                    .ifPresent(suggestion -> paramhelp.append(" ").append(suggestion));

                throw new BasicError(paramhelp.toString());
            }
            Object value = config.get(configkey);
            Object testValue = convertValueTo(ofType.getSimpleName(), configkey, value, element.getType());
        }
    }

    @Override
    public ConfigModel add(NBConfigModel otherModel) {
        for (Param<?> param : otherModel.getParams()) {
            add(param);
        }
        return this;
    }
}
