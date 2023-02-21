/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.api.config.params;

import io.nosqlbench.api.content.NBIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class DataSources {

    private final static Logger logger = LogManager.getLogger(DataSources.class);

    private static final List<ConfigSource> sources = List.of(
        new MapBackedConfigSource(),
        new JsonConfigSource(),
        new ParamsParserSource(),
        new ListBackedConfigSource()
    );

    public static List<ElementData> elements(Object src) {
        return elements(null, src);
    }

    public static List<ElementData> elements(String name, Object src) {

        if (src instanceof CharSequence && src.toString().startsWith("IMPORT{") && src.toString().endsWith("}")) {
            String data = src.toString();
            String filename = data.substring("IMPORT{".length(), data.length() - 1);
            Path filepath = Path.of(filename);

            src = NBIO.all().pathname(filename).first()
                .map(c -> {
                    logger.debug(() -> "found 'data' at " + c.getURI());
                    return c.asString();
                }).orElseThrow();
        }

        if (src instanceof ElementData) {
            return List.of((ElementData) src);
        }

        for (ConfigSource source : sources) {
            if (source.canRead(src)) {
                List<ElementData> elements = source.getAll(name, src);
                return elements;
            }
        }

        throw new RuntimeException("Unable to find a config reader for source type " + src.getClass().getCanonicalName());

    }

    public static ElementData element(Object object) {
        return element(null, object);
    }
    public static ElementData element(String name, Object object) {
        List<ElementData> elements = elements(name, object);
        if (elements.size() != 1) {
            throw new RuntimeException("Expected exactly one object, but found " + elements.size());
        }
        return elements.get(0);
    }
}
