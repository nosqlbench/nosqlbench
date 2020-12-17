package io.nosqlbench.nb.api.config;

import io.nosqlbench.nb.api.Environment;

import java.util.LinkedHashMap;
import java.util.Optional;

public class ConfigReader extends LinkedHashMap<String, Object> {
    private final ConfigModel configModel;

    public ConfigReader(ConfigModel model, LinkedHashMap<String, Object> validConfig) {
        super(validConfig);
        this.configModel = model;
    }

    public <T> T paramEnv(String name, Class<? extends T> vclass) {
        T param = param(name, vclass);
        if (param instanceof String) {
            Optional<String> interpolated = Environment.INSTANCE.interpolate(param.toString());
            if (interpolated.isEmpty()) {
                throw new RuntimeException("Unable to interpolate env and sys props in '" + param + "'");
            }
            return (T) interpolated.get();
        } else {
            return param;
        }

    }

    public <T> T param(String name, Class<? extends T> vclass) {
        Object o = get(name);
        ConfigElement<?> elem = configModel.getElements().get(name);
        if (elem == null) {
            throw new RuntimeException("Invalid config element named '" + name + "'" );
        }
        Class<T> type = (Class<T>) elem.getType();
        T typeCastedValue = type.cast(o);
        return typeCastedValue;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.configModel.getOf().getSimpleName()).append(":" );
        sb.append(this.configModel.toString());
        return sb.toString();

    }
}
