/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.nb.api.config.params;

import com.google.gson.*;

import java.util.*;

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
    public Object getAsCommon(String key) {
        Object found = get(key);
        if (found==null) {
            return null;
        }

        Optional<Object> converted = ElementData.asCommonType(found);
        if (converted.isPresent()) {
            return converted.get();
        }

        if (found instanceof JsonObject jo) {
            Map<String,Object> values = new LinkedHashMap<>();
            for (String s : jo.keySet()) {
                values.put(s, toCommon(jo.get(s)));
            }
            return values;
        }
        return toCommon(found);
    }

    public Object toCommon(Object srcValue) {
        Optional<Object> standards = ElementData.asCommonType(srcValue);
        if (standards.isPresent()) {
            return Optional.of(standards);
        }

        if (srcValue instanceof JsonElement e) {
            if (e.isJsonPrimitive()) {
                JsonPrimitive jp = e.getAsJsonPrimitive();
                if (jp.isBoolean()) {
                    return jp.getAsBoolean();
                } else if (jp.isNumber()) {
                    Number number = jp.getAsNumber();
                    return number.doubleValue();
                } else if (jp.isString()) {
                    return jp.getAsString();
                } else if (jp.isJsonNull()) {
                    return null;
                } else {
                    throw new RuntimeException("Unknown JSON primitive type for element: '" +
                        e
                        + "' type:'"
                        +e.getClass().getCanonicalName()+"'"
                    );
                }
            } else if (e.isJsonObject()) {
                JsonObject jo  = e.getAsJsonObject();
                Map<String,Object> valueMap = new LinkedHashMap<>();
                for (String s : jo.keySet()) {
                    valueMap.put(s, toCommon(jo.get(s)));
                }
                return valueMap;
            } else if (e.isJsonArray()) {
                List<Object> valueList = new ArrayList<>();
                JsonArray ja = e.getAsJsonArray();
                for (JsonElement jsonElement : ja) {
                    valueList.add(toCommon(jsonElement));
                }
                return valueList;
            }
        } else {
            throw new RuntimeException("Error traversing JSONElement structure. Unknown type: '"
                + srcValue.getClass().getCanonicalName()
                + "'");
        }

        throw new RuntimeException("Unable to convert value type from '"
            + srcValue.getClass().getCanonicalName() + "' to a common type.");

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
