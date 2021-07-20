package io.nosqlbench.nb.api.config.standard;

import io.nosqlbench.nb.api.NBEnvironment;
import io.nosqlbench.nb.api.errors.BasicError;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class NBConfiguration {
    private final LinkedHashMap<String, Object> data;
    private final NBConfigModel configModel;

    /**
     * Create a NBConfigReader from a known valid configuration and a config model.
     * This method is restricted to encourage construction of readers only by passing
     * through the friendly {@link NBConfigModel#apply(Map)} method.
     *
     * @param model       A configuration model, describing what is allowed to be configured by name and type.
     * @param validConfig A valid config reader.
     */
    protected NBConfiguration(NBConfigModel model, LinkedHashMap<String, Object> validConfig) {
        this.data = validConfig;
        this.configModel = model;
    }

    /**
     * Returns the value of the named parameter as {@link #getOptional(String)}, so long
     * as no env vars were reference OR all env var references were found.
     * @param name The name of the variable to look up
     * @return An optional value, if present and (optionally) interpolated correctly from the environment
     */
    public Optional<String> getEnvOptional(String name) {
        Optional<String> optionalValue = getOptional(name);
        if (optionalValue.isEmpty()) {
            return Optional.empty();
        }
        String span = optionalValue.get();
        Optional<String> maybeInterpolated = NBEnvironment.INSTANCE.interpolate(span);
        if (maybeInterpolated.isEmpty()) {
            throw new BasicError("Unable to interpolate '" + span +"' with env vars.");
        }
        return maybeInterpolated;
    }

    public String paramEnv(String name) {
        return paramEnv(name, String.class);
    }

    public <T> T paramEnv(String name, Class<? extends T> vclass) {
        T param = param(name, vclass);
        if (param instanceof String) {
            Optional<String> interpolated = NBEnvironment.INSTANCE.interpolate(param.toString());
            if (interpolated.isEmpty()) {
                throw new RuntimeException("Unable to interpolate env and sys props in '" + param + "'");
            }
            return (T) interpolated.get();
        } else {
            return param;
        }

    }

    public <T> T get(String name, Class<? extends T> type) {
        if (!configModel.getElements().containsKey(name)) {
             throw new BasicError("Parameter named '" + name + "' is not valid for " + configModel.getOf().getSimpleName() + ".");
        }
        Object o = data.get(name);
        if (o == null) {
            throw new BasicError("config param '" + name + "' was not defined.");
        }
        if (type.isAssignableFrom(o.getClass())) {
            return (T) o;
        }
        throw new BasicError("config param '" + name + "' was not assignable to class '" + type.getCanonicalName() + "'");
    }

    public Optional<String> getOptional(String name) {
        return getOptional(new String[]{name});
    }

    public Optional<String> getOptional(String... names) {
        return getOptional(String.class, names);
    }

    public <T> Optional<T> getOptional(Class<T> type, String... names) {
        Object o = null;
        for (String name : names) {
            o = data.get(name);
            if (o!=null) {
                break;
            }
        }
        if (o==null) {
            return Optional.empty();
        }
        if (type.isAssignableFrom(o.getClass())) {
            return Optional.of((T) o);
        }
        throw new BasicError("config param " + Arrays.toString(names) +" was not assignable to class '" + type.getCanonicalName() + "'");
    }

    public <T> T getOrDefault(String name, T defaultValue) {
        Object o = data.get(name);
        if (o == null) {
            return defaultValue;
        }
        if (defaultValue.getClass().isAssignableFrom(o.getClass())) {
            return (T) o;
        }
        throw new BasicError("config parameter '" + name + "' is not assignable to required type '" + defaultValue.getClass() + "'");
    }

    public <T> T param(String name, Class<? extends T> vclass) {
        Object o = data.get(name);
        Param<?> elem = configModel.getElements().get(name);
        if (elem == null) {
            throw new RuntimeException("Invalid config element named '" + name + "'");
        }
        Class<T> type = (Class<T>) elem.getType();
        T typeCastedValue = type.cast(o);
        return typeCastedValue;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.configModel.getOf().getSimpleName()).append(":");
        sb.append(this.configModel);
        return sb.toString();

    }

    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    public Map<String, Object> getMap() {
        return data;
    }

}
