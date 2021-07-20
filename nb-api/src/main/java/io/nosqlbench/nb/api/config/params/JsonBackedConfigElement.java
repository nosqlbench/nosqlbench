package io.nosqlbench.nb.api.config.params;

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
