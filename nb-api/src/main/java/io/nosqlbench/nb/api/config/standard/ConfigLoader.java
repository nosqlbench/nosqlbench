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

package io.nosqlbench.nb.api.config.standard;

import com.google.gson.*;
import io.nosqlbench.nb.api.config.params.ParamsParser;
import io.nosqlbench.nb.api.content.NBIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * <P>The config loader is meant to be the way that configurations
 * for objects or subsystems are loaded generically.</P>
 *
 * <p>It supports value which are defined as JSON objects, lists
 * of JSON objects, or as a fall-back simple parameter maps according to
 * {@link ParamsParser} rules.</p>
 *
 * If a block of config data begins with a '[' (open square bracket),
 * it is taken as a JSON list of configs. If it starts with a '{' (open curly
 * brace), it is taken as a single config. Otherwise it is taken as a simple
 * set of named parameters using '=' as an assignment operator.
 *
 * An empty string represents the null value.
 *
 * Users of this interface should be prepared to receive a null, or a list
 * of zero or more config elements of the requested type.
 *
 * <H1>Importing configs</H1>
 * <p>
 * Configs can be imported from local files, classpath resources, or URLs.
 * This is supported with the form of <pre>{@code IMPORT{URL}}</pre>
 * where URL can be any form mentioned above. This syntax is obtuse and
 * strange, but the point of this is to use something
 * that should never occur in the wild, to avoid collisions with actual
 * configuration content, but which is also clearly doing what it says.</p>
 */
public class ConfigLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static Logger logger = LogManager.getLogger("CONFIG");

    /**
     * Load a string into an ordered map of objects, with the key being defined
     * by an extractor function over the objects. Any duplicate keys are treated
     * as an error. This is a useful method for loading configuration blocks
     * which must be distinctly named.
     *
     * @param source The config data
     * @param type   The type of configuration object to be stored in the map values
     * @param keyer  The function that extracts the key
     * @param <V>    The generic parameter for the type field
     * @return A map of named configuration objects
     */
    public <V> LinkedHashMap<String, V> loadMap(
            CharSequence source,
            Class<? extends V> type,
            Function<V, String> keyer) {

        LinkedHashMap<String, V> mapOf = new LinkedHashMap<>();
        List<V> elems = load(source, type);

        for (V elem : elems) {
            String key = keyer.apply(elem);
            if (mapOf.containsKey(key)) {
                throw new RuntimeException("Duplicitous key mappings are disallowed here.");
            }
            mapOf.put(key, elem);
        }
        return mapOf;
    }

    public <T> List<T> load(CharSequence source, Class<? extends T> type) {
        List<T> cfgs = new ArrayList<>();

        String data = source.toString();
        data = data.trim();
        if (data.isEmpty()) {
            return null;
        }

        if (data.startsWith("IMPORT{") && data.endsWith("}")) {
            String filename = data.substring("IMPORT{".length(), data.length() - 1);
            Path filepath = Path.of(filename);

            data = NBIO.all().name(filename).first()
                    .map(c -> {
                        logger.debug("found 'data' at " + c.getURI());
                        return c.asString();
                    }).orElseThrow();
        }

        if (data.startsWith("{") || data.startsWith("[")) {
            JsonParser parser = new JsonParser();

            JsonElement jsonElement = parser.parse(data);
            if (jsonElement.isJsonArray()) {
                JsonArray asJsonArray = jsonElement.getAsJsonArray();
                for (JsonElement element : asJsonArray) {
                    T object = gson.fromJson(element, type);
                    cfgs.add(object);
                }
            } else if (jsonElement.isJsonObject()) {
                cfgs.add(gson.fromJson(jsonElement, type));
            }
        } else if (Map.class.isAssignableFrom(type)) {
            Map<String, String> parsedMap = ParamsParser.parse(data, false);
            cfgs.add(type.cast(parsedMap));
        }
        return cfgs;
    }
}
