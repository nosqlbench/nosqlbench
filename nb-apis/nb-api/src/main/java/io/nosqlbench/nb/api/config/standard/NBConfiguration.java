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

import io.nosqlbench.nb.api.system.NBEnvironment;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class NBConfiguration {

    private final LinkedHashMap<String, Object> data;
    private final NBConfigModel model;

    /**
     * Create a NBConfigReader from a known valid configuration and a config model.
     * This method is restricted to encourage construction of readers only by passing
     * through the friendly {@link NBConfigModel#apply(Map)} method.
     *
     * @param model
     *     A configuration model, describing what is allowed to be configured by name and type.
     * @param validConfig
     *     A valid config reader.
     */
    protected NBConfiguration(NBConfigModel model, LinkedHashMap<String, Object> validConfig) {
        this.data = validConfig;
        this.model = model;
    }

    public NBConfigModel getModel() {
        return model;
    }

    public static NBConfiguration empty() {
        return new NBConfiguration(ConfigModel.of(Object.class).asReadOnly(), new LinkedHashMap<>());
    }

    /**
     * Returns the value of the named parameter as {@link #getOptional(String)}, so long
     * as no env vars were reference OR all env var references were found.
     *
     * @param name
     *     The name of the variable to look up
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
            throw new NBConfigError("Unable to interpolate '" + span + "' with env vars.");
        }
        return maybeInterpolated;
    }

    public String getWithEnv(String name) {
        return getWithEnv(name, String.class);
    }

    public <T> T getWithEnv(String name, Class<? extends T> vclass) {
        T value = get(name, vclass);
        if (value == null) {

        }
        if (value instanceof String) {
            Optional<String> interpolated = NBEnvironment.INSTANCE.interpolate(value.toString());
            if (interpolated.isEmpty()) {
                throw new NBConfigError("Unable to interpolate env and sys props in '" + value + "'");
            }
            String result = interpolated.get();
            return ConfigModel.convertValueTo(this.getClass().getSimpleName(), name, result, vclass);
        } else {
            return value;
        }
    }

    /**
     * Get a config value or object by name. This uses type inference (as a generic method)
     * in addition to the internal model for type checking and ergonomic use. If you do not
     * call this within an assignment or context where the Java compiler knows what type you
     * are expecting, then use {@link #get(String, Class)} instead.
     *
     * @param name
     *     The name of the configuration parameter
     * @param <T>
     *     The (inferred) generic type of the configuration value
     * @return The value of type T, matching the config model type for the provided field name
     */
    public <T> T get(String name) {
        Param<T> param = (Param<T>) model.getNamedParams().get(name);
        if (param == null) {
            throw new NBConfigError("Attempted to get parameter for name '" + name + "' but this parameter has no " +
                "model defined for " + this.getModel().getOf());
        }
//        if (param.isRequired() && (param.getDefaultValue()==null) && )
        Object object = this.data.get(name);
        object = object != null ? object : param.getDefaultValue();
        if (object == null && param.isRequired()) {
            throw new NBConfigError("An object by name '" + name + "' was requested as required, and no value was" +
                " defined for it. This user provided value must be set or otherwise marked optional or given a" +
                " default value in the parameter model.");
        } else if (object == null && !param.isRequired()) {
            throw new NBConfigError("An object by name '" + name + "' was requested as given by the config layer," +
                " but no value was present, and no default was found in the config model. This is an ambiguous " +
                "scenario. Either access the object as optional, or give it a default value. (code change)");
        }
        if (param.type.isInstance(object)) {
            return (T) object;
        } else if (param.type.isAssignableFrom(object.getClass())) {
            return param.type.cast(object);
        } else if (NBTypeConverter.canConvert(object, param.type)) {
            return NBTypeConverter.convert(object, param.type);
        } else {
            throw new NBConfigError("Unable to assign config value for field '" + name + "' of type '" + object.getClass().getCanonicalName() + "' to the required return type '" + param.type.getCanonicalName() + "' as specified in the config model for '" + model.getOf().getCanonicalName());
        }
    }

    public <T> T get(String name, Class<? extends T> type) {

        Param<T> param = model.getParam(name);
        if (param == null) {
            throw new NBConfigError("Parameter named '" + name + "' is not valid for " + model.getOf().getSimpleName() + ".");
        }

        if ((!param.isRequired()) && param.getDefaultValue() == null) {
            throw new RuntimeException("Non-optional get on optional parameter " + name + "' which has no default value while configuring " + model.getOf() + "." +
                "\nTo avoid user impact, ensure that ConfigModel and NBConfigurable usage are aligned.");
        }

        Object o = data.get(name);
        if (o == null) {
            if (param.getDefaultValue() == null) {
                throw new NBConfigError("config param '" + name + "' was not defined.");
            } else {
                o = param.getDefaultValue();
            }
        }
        return ConfigModel.convertValueTo(this.getClass().getSimpleName(), name, o, type);
    }

    public Optional<String> getOptional(String name) {
        return getOptional(new String[]{name});
    }

    public Optional<String> getOptional(String... names) {
        return getOptional(String.class, names);
    }

    public <T> Optional<T> getOptional(Class<T> type, String... names) {
        Object o = null;
        Param<?> param = null;
        for (String name : names) {
            param = model.getParam(names);
            if (param != null) {
                for (String pname : param.getNames()) {
                    o = data.get(pname);
                    if (o != null) {
                        break;
                    }
                }
            } else {
                throw new NBConfigError("Parameter was not found for " + Arrays.toString(names) + ".");
            }
        }
        if (o == null) {
            if (param != null && param.isRequired()) {
                o = param.getDefaultValue();
            } else {
                return Optional.empty();
            }
        }
        if (type.isInstance(o)) {
            return Optional.of((T) o);
        } else if (type.isAssignableFrom(o.getClass())) {
            return Optional.of((T) type.cast(o));
        } else if (NBTypeConverter.canConvert(o, type)) {
            return Optional.of((T) NBTypeConverter.convert(o, type));
        } else {
            throw new NBConfigError("config param " + Arrays.toString(names) + " was not assignable to class '" + type.getCanonicalName() + "'");
        }

    }

    public <T> T getOrDefault(String name, T defaultValue) {
        Object o = data.get(name);
        if (o == null) {
            return defaultValue;
        }
        if (defaultValue.getClass().isAssignableFrom(o.getClass())) {
            return (T) o;
        }
        throw new NBConfigError("config parameter '" + name + "' is not assignable to required type '" + defaultValue.getClass() + "'");
    }

    public <T> T param(String name, Class<? extends T> vclass) {
        Object o = data.get(name);
        Param<?> elem = model.getNamedParams().get(name);
        if (elem == null) {
            throw new NBConfigError("Invalid config element named '" + name + "'");
        }
        Class<T> type = (Class<T>) elem.getType();
        T typeCastedValue = type.cast(o);
        return typeCastedValue;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.model.getOf().getSimpleName()).append(":");
        sb.append(this.model);
        return sb.toString();

    }

    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    public LinkedHashMap<String, Object> getMap() {
        return data;
    }

}
