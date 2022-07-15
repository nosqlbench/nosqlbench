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

package io.nosqlbench.converters.cql.exporters.transformers;

import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Combinations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class NameRemapper {
    private final LongFunction<String> namefunc;
    private final Map<String,String> remapped = new HashMap<>();
    private long index=0;

    public NameRemapper() {
        this.namefunc = new Combinations("a-z;a-z;a-z;a-z;a-z;a-z;");
    }
    public NameRemapper(LongFunction<String> function) {
        this.namefunc = function;
    }

    public synchronized String nameFor(NBNamedElement element) {
        String canonical = element.getClass().getSimpleName()+"--"+element.getName();
        if (!remapped.containsKey(canonical)) {
            String newname = namefunc.apply(index++);
            remapped.put(canonical,newname);
        }
        return remapped.get(canonical);
    }
}
