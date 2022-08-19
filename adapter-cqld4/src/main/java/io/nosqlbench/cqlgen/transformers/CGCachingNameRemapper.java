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

package io.nosqlbench.cqlgen.transformers;

import io.nosqlbench.api.labels.Labeled;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_string.Combinations;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongFunction;

public class CGCachingNameRemapper {
    private LongFunction<String> namefunc;
    private final Map<String,String> remapped = new HashMap<>();
    private final Map<String,String> prefixmap = new HashMap<>();
    private final Map<String,Long> indexmap = new HashMap<>();

    public CGCachingNameRemapper() {
        this.namefunc = new Combinations("a-z;a-z;a-z;a-z;a-z;a-z;");
    }
    public CGCachingNameRemapper(LongFunction<String> function) {
        this.namefunc = function;
    }

//    public synchronized String nameForType(String type, String originalName, String prefix) {
//        String canonical = type+"_"+originalName;
//        return getOrCreateName(canonical, prefix);
//    }

//    public synchronized String nameFor(NBNamedElement element) {
//        String prefix = prefixmap.get(element.getClass().getSimpleName());
//        String canonical = element.getClass().getSimpleName()+"-"+element.getName();
//        return getOrCreateName(canonical, prefix);
//    }


    private long indexforType(String type) {
        long newvalue = indexmap.computeIfAbsent(type, t -> 0L)+1;
        indexmap.put(type,newvalue);
        return newvalue;
    }

    public synchronized String nameFor(Map<String,String> labels) {
        String type = labels.get("type");
        Objects.requireNonNull(type);
        String name = labels.get("name");
        Objects.requireNonNull(name);
        String canonical = type+"-"+name;
        String prefix = prefixmap.getOrDefault(type,"");
        if (!remapped.containsKey(canonical)) {
            long indexForType=indexforType(type);
            String newname = (prefix!=null?prefix:"")+namefunc.apply(indexForType);
            remapped.put(canonical,newname);
        }
        return remapped.get(canonical);
    }

    public synchronized String nameFor(Labeled element) {
        Map<String, String> labels = element.getLabels();
        return nameFor(labels);
    }

    //    public Function<String, String> mapperForType(Labeled cqlTable, String prefix) {
//        return in -> this.nameForType(cqlTable.getClass().getSimpleName(),in, prefix);
//    }
//
    public void setNamingFunction(LongFunction<String> namerFunc) {
        this.namefunc = namerFunc;
    }
    public void setTypePrefixes(Map<String,String> prefixesByLabeledType) {
        this.prefixmap.clear();
        this.prefixmap.putAll(prefixesByLabeledType);
    }
}
