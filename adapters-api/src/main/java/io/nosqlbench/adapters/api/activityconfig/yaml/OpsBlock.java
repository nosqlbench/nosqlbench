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

package io.nosqlbench.adapters.api.activityconfig.yaml;

import io.nosqlbench.adapters.api.activityconfig.MultiMapLookup;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpDef;
import io.nosqlbench.adapters.api.activityconfig.rawyaml.RawOpsBlock;
import io.nosqlbench.api.engine.util.Tagged;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OpsBlock implements Tagged, Iterable<OpTemplate> {

    private final RawOpsBlock rawOpsBlock;
    private final OpsDoc rawOpsDoc;
    private final int blockIdx;


    public OpsBlock(RawOpsBlock rawOpsBlock, OpsDoc rawOpsDoc, int blockIdx) {
        this.rawOpsBlock = rawOpsBlock;
        this.rawOpsDoc = rawOpsDoc;
        this.blockIdx = blockIdx;
    }

    public List<OpTemplate> getOps() {

        List<OpTemplate> rawOpTemplates = new ArrayList<>();
        List<RawOpDef> opDefs = rawOpsBlock.getRawOpDefs();

        for (int i = 0; i < opDefs.size(); i++) {
            rawOpTemplates.add(
                new OpDef(this, opDefs.get(i))
            );
        }
        return rawOpTemplates;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        if (!rawOpsDoc.getName().isEmpty()) {
            sb.append(rawOpsDoc.getName()).append("--");
        }
        if (!rawOpsBlock.getName().isEmpty()) {
            sb.append(rawOpsBlock.getName());
        } else {
            sb.append("block").append(blockIdx);
        }
        return sb.toString();
    }

    public Map<String, String> getTags() {
        return new MultiMapLookup<>(rawOpsBlock.getTags(), rawOpsDoc.getTags());
    }

    public Map<String, Object> getParams() {
        return new MultiMapLookup<>(rawOpsBlock.getParams(), rawOpsDoc.getParams());
    }

    public Map<String, String> getParamsAsText() {
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawOpsBlock.getParams(), rawOpsDoc.getParams());
        LinkedHashMap<String, String> stringmap = new LinkedHashMap<>();
        lookup.forEach((k, v) -> stringmap.put(k, v.toString()));
        return stringmap;
    }

    @SuppressWarnings("unchecked")
    public <V> V getParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawOpsBlock.getParams(), rawOpsDoc.getParams());
        if (!lookup.containsKey(name)) {
            return defaultValue;
        }
        Object value = lookup.get(name);
        return (V) defaultValue.getClass().cast(value);
    }

    public <V> V getParam(String name, Class<? extends V> type) {
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawOpsBlock.getParams(), rawOpsDoc.getParams());
        Object object = lookup.get(name);
        V value = type.cast(object);
        return value;
    }

    public Map<String, String> getBindings() {
        return new MultiMapLookup<>(rawOpsBlock.getBindings(), rawOpsDoc.getBindings());
    }

    @Override
    @NotNull
    public Iterator<OpTemplate> iterator() {
        return getOps().iterator();
    }
}
