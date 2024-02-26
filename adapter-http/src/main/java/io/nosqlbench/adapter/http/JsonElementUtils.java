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

import java.sql.Array;
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

    public static List<Float> customNumberArrayToFloatList(JsonElement element) {
        JsonObject o1 = element.getAsJsonObject();
        JsonElement data = o1.get("data");
        JsonArray dary = data.getAsJsonArray();
        JsonElement element0 = dary.get(0);
        JsonObject eobj1 = element0.getAsJsonObject();
        JsonElement embedding = eobj1.get("embedding");
        JsonArray ary = embedding.getAsJsonArray();
        ArrayList<Float> list = new ArrayList<>(ary.size());
        for (JsonElement jsonElement : ary) {
            list.add(jsonElement.getAsFloat());
        }
        return list;
    }

    public static float[] customNumberArrayToFloatArray(JsonElement element) {
        JsonObject o1 = element.getAsJsonObject();
        JsonElement data = o1.get("data");
        JsonArray dary = data.getAsJsonArray();
        JsonElement element0 = dary.get(0);
        JsonObject eobj1 = element0.getAsJsonObject();
        JsonElement embedding = eobj1.get("embedding");
        JsonArray ary = embedding.getAsJsonArray();
        float[] floats = new float[ary.size()];
        for (int i = 0; i < floats.length; i++) {
            floats[i]=ary.get(i).getAsFloat();
        }
        return floats;
    }

    public static float[][] customNumberArrayToFloatArrayBatch(JsonElement element) {
        JsonObject o1 = element.getAsJsonObject();
        JsonElement data = o1.get("data");
        JsonArray dary = data.getAsJsonArray();
        float[][] floats2dary = new float[dary.size()][];
        for (int vector_idx = 0; vector_idx < dary.size(); vector_idx++) {
            JsonElement element0 = dary.get(vector_idx);
            JsonObject eobj1 = element0.getAsJsonObject();
            JsonElement embedding = eobj1.get("embedding");
            JsonArray vectorAry = embedding.getAsJsonArray();
            float[] newV = new float[vectorAry.size()];
            for (int component_idx = 0; component_idx < vectorAry.size(); component_idx++) {
                newV[component_idx]=vectorAry.get(component_idx).getAsFloat();
            }
            floats2dary[vector_idx]=newV;
        }
        return floats2dary;
    }


}
