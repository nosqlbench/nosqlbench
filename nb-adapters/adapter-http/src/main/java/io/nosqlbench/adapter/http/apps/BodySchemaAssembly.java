/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapter.http.apps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.models.Model;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BodySchemaAssembly {
    private Map<String,String> bindingCache = new LinkedHashMap<>();
    public String assembleBodyTemplate(OpenAPI model, String elementName, Schema schema, String path, PathItem pathinfo, Operation op) {

        Object prototype = findPrototype(model, elementName, schema);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prototypeText = gson.toJson(prototype);
        return prototypeText;

    }

    private Object findPrototype(OpenAPI model, String name, Schema<?> schema) {
        return switch (schema) {
            case StringSchema str -> prototypeForString(model, name, str);
            case ObjectSchema obj -> prototypeForObject(model, name, obj);
            case ArraySchema ary -> prototypeForAry(model, name, ary);
            case ComposedSchema com -> prototypeForComposed(model, name, com);
            case NumberSchema num -> prototypeForNumber(model, name, num);
            case BinarySchema bin -> prototypeForBinary(model, name, bin);
            case MapSchema map -> prototypeForMap(model, name, map);
            case IntegerSchema intg -> prototypeForInt(model, name, intg);
            case BooleanSchema bs -> prototypeForBs(model, name, bs);
            default ->
                throw new RuntimeException("implement schema mapper for " + schema.getClass().getSimpleName());
        };
    }

    private Object prototypeForBs(OpenAPI model, String name, BooleanSchema bs) {
        bindingCache.put(name, OpenApiExporter.getRefOr(model,bs).getType());
        return "{" + name + "}";
    }

    private Object prototypeForInt(OpenAPI model, String name, IntegerSchema intg) {
        bindingCache.put(name, OpenApiExporter.getRefOr(model,intg).getType());
        return "{" + name + "}";
    }

    private Object prototypeForBinary(OpenAPI model, String name, BinarySchema bin) {
        bindingCache.put(name, OpenApiExporter.getRefOr(model,bin).getType());
        return "{" + name + "}";
    }

    private Object prototypeForNumber(OpenAPI model, String name, NumberSchema num) {
        bindingCache.put(name, OpenApiExporter.getRefOr(model,num).getType());
        return "{" + name + "}";
    }

    private String prototypeForString(OpenAPI model, String name, StringSchema schema) {
        bindingCache.put(name, OpenApiExporter.getRefOr(model,schema).getType());
        return "{" + name + "}";
    }

    private List<?> prototypeForAry(OpenAPI model, String name, ArraySchema ary) {
        List<Object> protolist = new ArrayList<>();

        Schema itemSchema = ary.getItems();
        if (itemSchema==null && ary.getOneOf()!=null && !ary.getOneOf().isEmpty()) {
            itemSchema = OpenApiExporter.getRefOr(model,ary.getOneOf().get(0));
        }
        if (itemSchema==null && ary.getAnyOf()!=null && !ary.getAnyOf().isEmpty()) {
            itemSchema = OpenApiExporter.getRefOr(model,ary.getAnyOf().get(0));
        }
        if (itemSchema==null) {
            throw new RuntimeException("Unable to determine item schema for ary: " + name + ", toString:" + ary);
        }
        protolist.add(findPrototype(model, name, OpenApiExporter.getRefOr(model,itemSchema)));
        return protolist;
    }

    private Object prototypeForMap(OpenAPI model, String name, MapSchema map) {
        Map<String, Object> protomap = new LinkedHashMap<>();
        Map<String, Schema> properties = map.getProperties();

        if (properties!=null && !properties.isEmpty()) {
            for (String pkey : properties.keySet()) {
                Schema ptype = OpenApiExporter.getRefOr(model,properties.get(pkey));
                protomap.put(pkey, findPrototype(model, pkey,ptype));
            }
        }
        return protomap;
    }

    private Map<String, ?> prototypeForObject(OpenAPI model, String name, ObjectSchema obj) {
        Map<String, Object> protomap = new LinkedHashMap<>();
        Map<String, Schema> properties = obj.getProperties();

        if (properties!=null && !properties.isEmpty()) {
            for (String pkey : properties.keySet()) {
                Schema ptype = OpenApiExporter.getRefOr(model,properties.get(pkey));
                protomap.put(pkey, findPrototype(model, pkey,ptype));
            }
        }
        return protomap;
    }

    private Object prototypeForComposed(OpenAPI model, String name, ComposedSchema cmp) {
        Map<String, Object> protomap = new LinkedHashMap<>();
        Map<String, Schema> properties =new LinkedHashMap<>();
        List<Schema> composition = cmp.getAllOf();
        if (composition==null) {
            List<Schema> anyof = cmp.getAnyOf();
            if (anyof!=null) {
                composition=List.of(OpenApiExporter.getRefOr(model,anyof.get(0)));
            }
        }
        if (composition==null) {
            List<Schema> oneOf = cmp.getOneOf();
            if (oneOf!=null) {
                composition=List.of(OpenApiExporter.getRefOr(model,oneOf.get(0)));
            }
        }
        if (composition.size()==1) {
            return findPrototype(model, name, composition.get(0));
        } else {
            for (String pkey : properties.keySet()) {
                Schema ptype = OpenApiExporter.getRefOr(model,properties.get(pkey));
                protomap.put(pkey, findPrototype(model, pkey,ptype));
            }
            return protomap;
        }

    }
    public Map<String,String> getBindingCache() {
        LinkedHashMap<String, String> toreturn = new LinkedHashMap<>();
        this.bindingCache.forEach((k,v) -> {
            String recipe = switch (v) {
                case "string" -> "ToString()";
                default -> "Identity()";
            };
            toreturn.put(k,recipe);
        });
        return toreturn;
    }

}
