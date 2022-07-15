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

package io.nosqlbench.engine.api.templating.binders;

import io.nosqlbench.engine.api.templating.ParsedTemplateMap;
import io.nosqlbench.api.errors.OpConfigError;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class OrderedMapBinder implements LongFunction<Map<String, Object>> {

    private final Map<String,Object> protomap = new LinkedHashMap<>();
    private final Map<String,LongFunction<?>> bindermap = new HashMap<>();

    public OrderedMapBinder(ParsedTemplateMap cmd, String... fields) {
        for (String field : fields) {
            if (cmd.isStatic(field)) {
                protomap.put(field,cmd.getStaticValue(field));
            } else if (cmd.isDynamic(field)) {
                bindermap.put(field,cmd.getMapper(field));
                protomap.put(field,null);
            } else {
                throw new OpConfigError("There was no field named " + field + " while building a MapBinder");
            }
        }
    }

    @Override
    public Map<String, Object> apply(long value) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>(protomap);
        bindermap.forEach((k,v) -> {
            map.put(k,v.apply(value));
        });
        return map;
    }
}
