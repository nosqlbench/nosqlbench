package io.nosqlbench.nb.api.config.params;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonBackedConfigElement implements ElementData {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final JsonObject jsonObject;

    public JsonBackedConfigElement(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public Object get(String name) {
        return jsonObject.get(name);
    }

    @Override
    public boolean containsKey(String name) {
        return jsonObject.keySet().contains(name);
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

}
