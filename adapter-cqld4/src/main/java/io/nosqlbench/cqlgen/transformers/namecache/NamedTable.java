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

package io.nosqlbench.cqlgen.transformers.namecache;

import io.nosqlbench.api.config.NBLabeledElement;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class NamedTable {
    private final String tablename;
    private final Map<String, NamedColumn> columns = new LinkedHashMap<>();
    private String alias;

    public NamedTable(final String tablename) {
        this.tablename = tablename;
    }

    public NamedColumn column(final String name) {
        return columns.computeIfAbsent(name, NamedColumn::new);
    }

    public NamedTable alias(final String alias) {
        this.alias = alias;
        return this;
    }

    public String computeAlias(final NBLabeledElement labeled, final Function<NBLabeledElement,String> namer) {
        if (null == this.alias) alias = namer.apply(labeled);
        return alias;
    }

    public String getAlias() {
        return alias;
    }

    public Collection<NamedColumn> columns() {
        return this.columns.values();
    }
}
