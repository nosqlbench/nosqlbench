/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.config.standard;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A configuration element describes a single configurable parameter.
 *
 * @param <T> The type of value which can be stored in this named configuration
 *            parameter in in actual configuration data.
 */
public class Param<T> {

    private final List<String> names;
    public final Class<? extends T> type;
    private String description;
    private final T defaultValue;
    public boolean required;
    private Pattern regex;
    private final NBConfigModelExpander expander;

    public Param(
        List<String> names,
        Class<? extends T> type,
        String description,
        boolean required,
        T defaultValue,
        NBConfigModelExpander expander
    ) {
        this.names = names;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
        this.expander = expander;
    }

    /**
     * Declare an optional String parameter with the given name.
     *
     * @param name the name of the parameter
     */
    public static Param<String> optional(String name) {
        return optional(List.of(name), String.class);
    }

    /**
     * Declare an optional String parameter specified by any of the names. They act as synonyms.
     * When users provide more than one of these in configuration data, it is considered an error.
     *
     * @param names one or more names that the parameter can be specified with.
     */
    public static Param<String> optional(List<String> names) {
        return optional(names, String.class);
    }

    /**
     * Declare an optional parameter specified by any of the names which must be assignable to
     * (returnable as) the specified type.
     * When users provide more than one of these in configuration data, it is considered an error.
     *
     * @param names one or more names that the parameter can be specified with.
     * @param type  The type of value that the provided configuration value must be returnable as (assignable to)
     * @param <V>   Generic type for inference.
     */
    public static <V> Param<V> optional(List<String> names, Class<V> type) {
        return new Param<V>(names, type, null, false, null, null);
    }

    /**
     * Declare an optional parameter specified by any of the names which must be assignable to
     * (returnable as) the specified type.
     * When users provide more than one of these in configuration data, it is considered an error.
     *
     * @param names       one or more names that the parameter can be specified with.
     * @param type        The type of value that the provided configuration value must be returnable as (assignable to)
     * @param description A description of what this parameter is
     * @param <V>         Generic type for inference.
     */
    public static <V> Param<V> optional(List<String> names, Class<V> type, String description) {
        return new Param<V>(names, type, description, false, null, null);
    }


    /**
     * Declare an optional parameter for the given name which must be assignable to
     * (returnable as) the specified type.
     * When users provide more than one of these in configuration data, it is considered an error.
     *
     * @param name the name of the parameter
     * @param type The type of value that the provided configuration value must be returnable as (assignable to)
     * @param <V>  Generic type for inference.
     */
    public static <V> Param<V> optional(String name, Class<V> type) {
        return new Param<V>(List.of(name), type, null, false, null, null);
    }

    /**
     * Declare an optional parameter for the given name which must be assignable to
     * (returnable as) the specified type.
     * When users provide more than one of these in configuration data, it is considered an error.
     *
     * @param name        the name of the parameter
     * @param type        The type of value that the provided configuration value must be returnable as (assignable to)
     * @param description A description of what this parameter is
     * @param <V>         Generic type for inference.
     */
    public static <V> Param<V> optional(String name, Class<V> type, String description) {
        return new Param<V>(List.of(name), type, description, false, null, null);
    }

    /**
     * Parameters which are given a default value are automatically marked as required, as the default
     * value allows them to be accessed as such.
     *
     * @param name
     * @param defaultValue
     * @param <V>
     * @return
     */
    public static <V> Param<V> defaultTo(String name, V defaultValue) {
        return new Param<V>(List.of(name), (Class<V>) defaultValue.getClass(), null, true, defaultValue, null);
    }

    /**
     * Parameters which are given a default value are automatically marked as required, as the default
     * value allows them to be accessed as such.
     *
     * @param name
     * @param defaultValue
     * @param <V>
     * @return
     */
    public static <V> Param<V> defaultTo(String name, V defaultValue, String description) {
        return new Param<V>(List.of(name), (Class<V>) defaultValue.getClass(), description, true, defaultValue, null);
    }

    /**
     * Parameters which are given a default value are automatically marked as required, as the default
     * value allows them to be accessed as such.
     *
     * @param names
     * @param defaultValue
     * @param <V>
     * @return
     */
    public static <V> Param<V> defaultTo(List<String> names, V defaultValue) {
        return new Param<V>(names, (Class<V>) defaultValue.getClass(), null, true, defaultValue, null);
    }

    public static <V> Param<V> required(String name, Class<V> type) {
        return new Param<V>(List.of(name), type, null, true, null, null);
    }

    public static <V> Param<V> required(List<String> names, Class<V> type) {
        return new Param<V>(names, type, null, true, null, null);
    }


    @Override
    public String toString() {
        return "Element{" +
            "names='" + names.toString() + '\'' +
            ", type=" + type +
            ", description='" + description + '\'' +
            ", required=" + required +
            ", defaultValue = " + defaultValue +
            '}';
    }

    public List<String> getNames() {
        return names;
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

    public Param<?> setRequired(boolean required) {
        this.required = required;
        return this;
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
                return CheckResult.INVALID(this, null, "Value is null but " + this.getNames() + " is required");
            } else {
                return CheckResult.VALID(this, null, "Value is null, but " + this.getNames() + " is not required");
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
                            ") with value '" + value + "' for field '" + getNames() + "'");
                }
            }
        }
        return CheckResult.VALID(this, value, "All validators passed for field '" + getNames() + "'");
    }


    public NBConfigModelExpander getExpander() {
        return this.expander;
    }

    public Param<T> expand(NBConfigModelExpander expander) {
        return new Param<>(names, type, description, required, defaultValue, expander);
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
