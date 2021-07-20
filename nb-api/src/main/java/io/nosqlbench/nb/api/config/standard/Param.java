package io.nosqlbench.nb.api.config.standard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A configuration element describes a single configurable parameter.
 *
 * @param <T> The type of value which can be stored in this named configuration
 *            parameter in in actual configuration data.
 */
public class Param<T> {

    public final String name;
    public final Class<? extends T> type;
    public String description;
    private final T defaultValue;
    public boolean required;
    private Pattern regex;

    public Param(
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

    public static <V> Param<V> optional(String name) {
        return (Param<V>) optional(name,String.class);
    }

    public static <V>  Param<V> optional(String name, Class<V> type) {
        return new Param<V>(name,type,null,false,null);
    }
    public static <V>  Param<V> defaultTo(String name, V defaultValue) {
        return new Param<V>(name,(Class<V>) defaultValue.getClass(),null,false,null);
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

    public Param<T> setDescription(String description) {
        this.description = description;
        return this;
    }

    public Param<T> setRegex(Pattern regex) {
        this.regex = regex;
        return this;
    }
    public Param<T> setRegex(String pattern) {
        this.regex = Pattern.compile(pattern);
        return this;
    }

    public Pattern getRegex() {
        return regex;
    }

    public CheckResult<T> validate(Object value) {


        if (value == null) {
            if (isRequired()) {
                return CheckResult.INVALID(this, null, "Value is null but " + this.getName() + " is required");
            } else {
                return CheckResult.VALID(this, null, "Value is null, but " + this.getName() + " is not required");
            }
        }

        if (!this.getType().isAssignableFrom(value.getClass())) {
            return CheckResult.INVALID(this, value, "Can't assign " + value.getClass().getSimpleName() + " to " + "" +
                this.getType().getSimpleName());
        }

        if (getRegex() != null) {
            if (value instanceof CharSequence) {
                Matcher matcher = getRegex().matcher(value.toString());
                if (!matcher.matches()) {
                    return CheckResult.INVALID(this, value,
                        "Could not match required pattern (" + getRegex().toString() +
                            ") with value '" + value + "' for field '" + getName() + "'");
                }
            }
        }
        return CheckResult.VALID(this,value,"All validators passed for field '" + getName() + "'");
    }

    public final static class CheckResult<T> {
        public final Param<T> element;
        public final Object value;
        public final String message;
        private final boolean isValid;

        private CheckResult(Param<T> e, Object value, String message, boolean isValid) {
            this.element = e;
            this.value = value;
            this.message = message;
            this.isValid = isValid;
        }

        public static <T> CheckResult<T> VALID(Param<T> element, Object value, String message) {
            return new CheckResult<>(element, value, message, true);
        }

        public static <T> CheckResult<T> VALID(Param<T> element, Object value) {
            return new CheckResult<>(element, value, "", true);
        }

        public static <T> CheckResult<T> INVALID(Param<T> element, Object value, String message) {
            return new CheckResult<>(element, value, message, false);
        }

        public boolean isValid() {
            return isValid;
        }
    }
}
