package io.nosqlbench.nb.api.config.params;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

public class JsonConfigSource implements ConfigSource {
    private final static Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
    public List<ElementData> getAll(Object data) {

        JsonElement element = null;

        // Pull JSON element from data
        if (data instanceof CharSequence) {
            JsonParser p = new JsonParser();
            element = p.parse(data.toString());
        } else if (data instanceof JsonElement) {
            element = (JsonElement) data;
        }

        // Handle element modally by type
        List<ElementData> readers = new ArrayList<>();


        if (element.isJsonArray()) {
            JsonArray ary = element.getAsJsonArray();
            for (JsonElement jsonElem : ary) {
                if (jsonElem.isJsonObject()) {
                    readers.add(new JsonBackedConfigElement(jsonElem.getAsJsonObject()));
                } else {
                    throw new RuntimeException("invalid object type for element in sequence: "
                        + jsonElem.getClass().getSimpleName());
                }
            }
        } else if (element.isJsonObject()) {
            readers.add(new JsonBackedConfigElement(element.getAsJsonObject()));
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String asString = element.getAsJsonPrimitive().getAsString();
            ElementData e = DataSources.element(asString);
            readers.add(e);
        } else {
            throw new RuntimeException("Invalid object type for element:" +
                element.getClass().getSimpleName());
        }


        return readers;
    }
//
//    @Override
//    public ElementData getOneElementData(Object src) {
//        JsonElement element = (JsonElement) src;
//
//
//    }

}
