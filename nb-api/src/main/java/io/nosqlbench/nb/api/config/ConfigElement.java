package io.nosqlbench.nb.api.config;

public class ConfigElement<T> {

    public final String name;
    public final Class<? extends T> type;
    public final String description;
    private final T defaultValue;
    public boolean required;

    public ConfigElement(
            String name,
            Class<? extends T> type,
            String description,
            boolean required,
            T defaultValue
    ) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", required=" + required +
                ", defaultValue = " + defaultValue +
                '}';
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public T getDefaultValue() {
        return defaultValue;
    }
}
