package io.nosqlbench.adapter.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class JsonElementUtils {

    /**
     * <Pre>{@code
     * "hits": {
     *     "hits": [
     *       {
     *         "_score": 1,
     *         "_id": "doGwOYsBv7KeAUqukb5D",
     *         "_source": {
     *           "key": 550,
     *           "value": [
     *             -0.34495,
     *             1.0193,
     *             0.87505,
     * }</Pre>
     * @param element
     * @return
     */
    public static int[] getIntArrayFromHits(JsonElement jsonElement) {
        JsonObject json = jsonElement.getAsJsonObject();

        if (!json.has("hits") || !json.getAsJsonObject("hits").has("hits")) {
            return null;
        }
        JsonArray hits = json.getAsJsonObject("hits").getAsJsonArray("hits");

        int count = hits.size();
        int[] keys = new int[count];
        int i = 0;
        for (JsonElement element : hits) {
            JsonObject hit = element.getAsJsonObject();
            keys[i] = hit.getAsJsonObject("_source").get("key").getAsInt();
            i++;
        }
        return keys;

    }
}
