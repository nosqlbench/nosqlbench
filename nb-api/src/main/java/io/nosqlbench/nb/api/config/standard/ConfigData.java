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

import io.nosqlbench.nb.api.errors.BasicError;

import java.util.*;

public class ConfigData {
    private final ConfigData inner;
    private final LinkedHashMap<String,Object> configs;

    public ConfigData(LinkedHashMap<String,Object> configs, ConfigData inner) {
        this.configs = configs;
        this.inner =inner;
    }

    public ConfigData(LinkedHashMap<String,Object> configs) {
        this.configs = configs;
        this.inner = null;
    }

    public ConfigData() {
        this.configs = new LinkedHashMap<>();
        this.inner = null;
    }

    public ConfigData layer() {
        return new ConfigData(new LinkedHashMap<>(),this);
    }

    public ConfigData layer(Map<String,Object> configs ){
        return new ConfigData(new LinkedHashMap<>(configs),this);
    }

    /**
     * Get the typed optional value for the requested parameter name.
     * @param name The name of the parameter to use
     * @param type The class type which the value must be assignable to.
     * @param <T> The generic parameter of the class type
     * @return An optional of type T
     * @throws BasicError if a value is found which can't be returned as the
     * specified type.
     */
    public <T> Optional<T> get(String name, Class<? extends T> type) {
        Object o = configs.get(name);
        if (o!=null) {
            if (type.isAssignableFrom(o.getClass())) {
                return Optional.of(type.cast(o));
            } else {
                throw new BasicError("Tried to access a virtdata config element named '" + name +
                    "' as a '" + type.getCanonicalName() + "', but it was not compatible with that type");
            }
        }
        if (inner !=null) {
            return inner.get(name, type);
        }
        return Optional.empty();
    }

    /**
     * Get the typed optional list for the requested list name. This is no different than
     * getting an object without the list qualifier, except that the type checking is done for
     * you internal to the getList method.
     * @param name The name of the parameter to return
     * @param type The type of the list element. Every element must be assignable to this type.
     * @param <T> The generic parameter of the list element type
     * @return An optional list of T
     * @throws BasicError if any of the elements are not assignable to the required element type
     */
    public <T> Optional<List<T>> getList(String name, Class<? extends T> type) {
        Optional<List> found = get(name, List.class);
        if (found.isPresent()) {
            ArrayList<T> list = new ArrayList<>();
            for (Object o : found.get()) {
                if (type.isAssignableFrom(o.getClass())) {
                    list.add(type.cast(o));
                } else {
                    throw new BasicError("Tried to access a virtdata config list element found under name '" + name +
                        "' as a '" + type.getCanonicalName() + "', but it was not compatible with that type");

                }
            }
            return Optional.of(list);
        }
        return Optional.empty();

    }

    public void put(String paramName, List<String> paramValue) {
        this.configs.put(paramName,paramValue);
    }
}
