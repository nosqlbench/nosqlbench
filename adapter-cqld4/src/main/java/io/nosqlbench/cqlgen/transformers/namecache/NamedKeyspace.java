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

package io.nosqlbench.cqlgen.transformers.namecache;

import io.nosqlbench.api.labels.Labeled;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class NamedKeyspace {
    private final String ksname;
    private final Map<String, NamedTable> tables = new LinkedHashMap<>();
    private final Map<String, NamedType> types = new LinkedHashMap<>();
    private String alias;

    public NamedKeyspace(String ksname) {
        this.ksname = ksname;
    }

    public NamedType type(String typename) {
        return types.computeIfAbsent(typename, NamedType::new);
    }

    public NamedTable table(String tablename) {
        return tables.computeIfAbsent(tablename, NamedTable::new);
    }

    public NamedKeyspace alias(String alias) {
        this.alias = alias;
        return this;
    }

    public String computeAlias(Labeled labeled, Function<Labeled,String> namer) {
        if (this.alias==null) {
            this.alias = namer.apply(labeled);
        }
        return this.alias;
    }

    public Collection<NamedTable> tables() {
        return tables.values();
    }

    public Collection<NamedType> types() {
        return types.values();
    }
}
