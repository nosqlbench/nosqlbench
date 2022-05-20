package io.nosqlbench.nb.api.config.standard;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.nb.api.NBEnvironment;

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
     * @param model       A configuration model, describing what is allowed to be configured by name and type.
     * @param validConfig A valid config reader.
     */
    protected NBConfiguration(NBConfigModel model, LinkedHashMap<String, Object> validConfig) {
        this.data = validConfig;
        this.model = model;
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
            throw new NBConfigError("Unable to interpolate '" + span +"' with env vars.");
        }
        return maybeInterpolated;
    }

    public String getWithEnv(String name) {
        return getWithEnv(name, String.class);
    }

    public <T> T getWithEnv(String name, Class<? extends T> vclass) {
        T value = get(name, vclass);
        if (value==null) {

        }
        if (value instanceof String) {
            Optional<String> interpolated = NBEnvironment.INSTANCE.interpolate(value.toString());
            if (interpolated.isEmpty()) {
                throw new NBConfigError("Unable to interpolate env and sys props in '" + value + "'");
            }
            String result = interpolated.get();
            return ConfigModel.convertValueTo(this.getClass().getSimpleName(),name, result, vclass);
        } else {
            return value;
        }

    }

    public String get(String name) {
        return get(name, String.class);
    }

    public <T> T get(String name, Class<? extends T> type) {
        Param<T> param = model.getParam(name);
        if (param==null) {
            throw new NBConfigError("Parameter named '" + name + "' is not valid for " + model.getOf().getSimpleName() + ".");
        }
        if ((!param.isRequired())&&param.getDefaultValue()==null) {
            throw new RuntimeException("Non-optional get on optional parameter " + name + "' which has no default value while configuring " + model.getOf() + "." +
                "\nTo avoid user impact, ensure that ConfigModel and NBConfigurable usage are aligned.");
        }

        Object o = data.get(name);
        if (o == null) {
            throw new NBConfigError("config param '" + name + "' was not defined.");
        }
        return ConfigModel.convertValueTo(this.getClass().getSimpleName(), name,o,type);
//        if (type.isAssignableFrom(o.getClass())) {
//            return (T) o;
//        }
//        throw new NBConfigError("config param '" + name + "' was not assignable to class '" + type.getCanonicalName() + "'");
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
            Param<?> param = model.getParam(names);
            if (param!=null) {
                for (String pname : param.getNames()) {
                    o =data.get(pname);
                    if (o!=null) {
                        break;
                    }
                }
            } else {
                throw new NBConfigError("Parameter was not found for " + Arrays.toString(names) + ".");
            }
        }
        if (o==null) {
            return Optional.empty();
        }
        if (type.isAssignableFrom(o.getClass())) {
            return Optional.of((T) o);
        }
        throw new NBConfigError("config param " + Arrays.toString(names) +" was not assignable to class '" + type.getCanonicalName() + "'");
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

    public Map<String, Object> getMap() {
        return data;
    }

}
