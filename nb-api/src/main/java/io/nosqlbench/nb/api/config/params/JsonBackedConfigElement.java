package io.nosqlbench.nb.api.config.params;

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


import com.google.gson.*;

import java.util.Set;

public class JsonBackedConfigElement implements ElementData {

    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final JsonObject jsonObject;
    private final String name;

    public JsonBackedConfigElement(String injectedName, JsonObject jsonObject) {
        this.name = injectedName;
        this.jsonObject = jsonObject;
    }

    @Override
    public Object get(String name) {
        return jsonObject.get(name);
    }

    @Override
    public Set<String> getKeys() {
        return jsonObject.keySet();
    }

    @Override
    public boolean containsKey(String name) {
        return jsonObject.keySet().contains(name);
    }

    @Override
    public String getGivenName() {
        return this.name;
    }

    @Override
    public <T> T convert(Object input, Class<T> type) {
        if (input instanceof JsonElement) {
            T result = gson.fromJson((JsonElement) input, type);
            return result;
        } else {
            throw new RuntimeException("Unable to convert json element from '" + input.getClass().getSimpleName() + "' to '" + type.getSimpleName() + "'");
        }
    }

    @Override
    public String toString() {
        return getGivenName() + "(" + (extractElementName()!=null ? extractElementName() : "null" ) +"):" + jsonObject.toString();
    }

    @Override
    public String extractElementName() {
        if (jsonObject.has("name")) {
            return jsonObject.get("name").getAsString();
        }
        return null;
    }
}
