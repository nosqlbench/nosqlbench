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

import java.util.ArrayList;
import java.util.List;

public class JsonConfigSource implements ConfigSource {
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String name;

    @Override
    public boolean canRead(Object data) {
        if (data instanceof JsonElement) {
            return true;
        }
        if (data instanceof CharSequence) {
            return (data.toString().startsWith("[") || data.toString().startsWith("{"));
        }
        return false;
    }

    @Override
    public List<ElementData> getAll(String name, Object data) {
        this.name = name;

        JsonElement element = null;

        // Pull JSON element from data
        if (data instanceof CharSequence) {
            JsonParser p = new JsonParser();
            element = p.parse(data.toString());
        } else if (data instanceof JsonElement) {
            element = (JsonElement) data;
        }

        // Handle element modally by type
        List<ElementData> elements = new ArrayList<>();


        if (element.isJsonArray()) {
            JsonArray ary = element.getAsJsonArray();
            for (JsonElement jsonElem : ary) {
                if (jsonElem.isJsonObject()) {
                    elements.add(new JsonBackedConfigElement(null, jsonElem.getAsJsonObject()));
                } else {
                    throw new RuntimeException("invalid object type for element in sequence: "
                        + jsonElem.getClass().getSimpleName());
                }
            }
        } else if (element.isJsonObject()) {
            elements.add(new JsonBackedConfigElement(null,element.getAsJsonObject()));
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String asString = element.getAsJsonPrimitive().getAsString();
            ElementData e = DataSources.element(name,asString);
            elements.add(e);
        } else {
            throw new RuntimeException("Invalid object type for element:" +
                element.getClass().getSimpleName());
        }

        return elements;
    }

    @Override
    public String getName() {
        return this.name;
    }
//
//    @Override
//    public ElementData getOneElementData(Object src) {
//        JsonElement element = (JsonElement) src;
//
//
//    }

}
