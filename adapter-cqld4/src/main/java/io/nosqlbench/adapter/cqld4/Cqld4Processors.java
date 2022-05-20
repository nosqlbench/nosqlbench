package io.nosqlbench.adapter.cqld4;

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


import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum Cqld4Processors {
    print(Cqld4PrintProcessor::new);

    private final Function<Map<String, ?>, ResultSetProcessor> initializer;

    Cqld4Processors(Function<Map<String,?>,ResultSetProcessor> initializer) {
        this.initializer = initializer;
    }

    public static ResultSetProcessor resolve(Map<String,?> cfg) {
        String type = Optional.ofNullable(cfg.get("type"))
            .map(Object::toString)
            .orElseThrow(() -> new RuntimeException("Map config provided for a processor, but with no type field."));

        Cqld4Processors procType = Cqld4Processors.valueOf(type);
        ResultSetProcessor p = procType.initializer.apply(cfg);
        return p;
    }
}
