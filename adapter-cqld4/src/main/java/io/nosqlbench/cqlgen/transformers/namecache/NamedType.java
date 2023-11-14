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

import io.nosqlbench.nb.api.labels.NBLabeledElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NamedType {
    private String name;
    private String alias;
    private final Map<String,NamedColumn> columns = new LinkedHashMap<>();

    public NamedType(final String typename) {
        name = typename;
    }

    public void alias(final String alias) {
        this.alias = alias;
    }

    public NamedColumn column(final String key) {
        return columns.computeIfAbsent(key, NamedColumn::new);
    }
    public List<NamedColumn> getColumnDefs() {
        return new ArrayList<>(this.columns.values());
    }

    public String computeAlias(final NBLabeledElement labeled, final Function<NBLabeledElement, String> namer) {
        if (null == this.alias) alias = namer.apply(labeled);
        return alias;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
