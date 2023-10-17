package io.nosqlbench.adapter.http;

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
