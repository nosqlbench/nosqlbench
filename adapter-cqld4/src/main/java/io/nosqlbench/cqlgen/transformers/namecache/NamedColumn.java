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

import java.util.Map;
import java.util.function.Function;

public class NamedColumn{
    private final String name;
    private String alias;

    public NamedColumn(final String name) {
        this.name = name;
    }

    public void alias(final String alias) {
        this.alias = alias;
    }

    public String computeAlias(final NBLabeledElement labeled, final Function<NBLabeledElement, String> namer) {
        if (null == this.alias) alias = namer.apply(labeled);
        return alias;
    }

    public String computeAlias(final Map<String,String> labels, final Function<Map<String,String>,String> namer) {
        if (null == this.alias) alias = namer.apply(labels);
        return alias;
    }

}
