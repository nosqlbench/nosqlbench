/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.hdf5.helpers;

import java.util.HashMap;
import java.util.Map;

public class EmbeddingGeneratorFactory {
    private static final Map<String, EmbeddingGenerator> generators = new HashMap<>();

    public static EmbeddingGenerator getGenerator(String type) {
        String typeLower = type.equalsIgnoreCase("short") ? "int" : type.toLowerCase();
        switch (typeLower) {
            case "float" -> {
                if (!generators.containsKey(type)) {
                    generators.put(type, new FloatEmbeddingGenerator());
                }
                return generators.get(type);
            }
            case "int" -> {
                if (!generators.containsKey(type)) {
                    generators.put(type, new IntEmbeddingGenerator());
                }
                return generators.get(type);
            }
            case "double" -> {
                if (!generators.containsKey(type)) {
                    generators.put(type, new DoubleEmbeddingGenerator());
                }
                return generators.get(type);
            }
            case "long" -> {
                if (!generators.containsKey(type)) {
                    generators.put(type, new LongEmbeddingGenerator());
                }
                return generators.get(type);
            }
            default -> throw new RuntimeException("Unknown embedding type: " + type);
        }
    }
}
