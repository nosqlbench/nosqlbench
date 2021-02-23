/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig.yaml;

import io.nosqlbench.engine.api.activityconfig.MultiMapLookup;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtDef;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsBlock;
import io.nosqlbench.engine.api.util.Tagged;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StmtsBlock implements Tagged, Iterable<OpTemplate> {

    private final static String NameToken = "name";
    private final static String StmtToken = "stmt";
    private final RawStmtsBlock rawStmtsBlock;
    private final StmtsDoc rawStmtsDoc;
    private final int blockIdx;


    public StmtsBlock(RawStmtsBlock rawStmtsBlock, StmtsDoc rawStmtsDoc, int blockIdx) {
        this.rawStmtsBlock = rawStmtsBlock;
        this.rawStmtsDoc = rawStmtsDoc;
        this.blockIdx = blockIdx;
    }

    public List<OpTemplate> getOps() {

        List<OpTemplate> rawOpTemplates = new ArrayList<>();
        List<RawStmtDef> statements = rawStmtsBlock.getRawStmtDefs();

        for (int i = 0; i < statements.size(); i++) {
            rawOpTemplates.add(
                new OpDef(this, statements.get(i))
            );
        }
        return rawOpTemplates;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        if (!rawStmtsDoc.getName().isEmpty()) {
            sb.append(rawStmtsDoc.getName()).append("--");
        }
        if (!rawStmtsBlock.getName().isEmpty()) {
            sb.append(rawStmtsBlock.getName());
        } else {
            sb.append("block").append(blockIdx);
        }
        return sb.toString();
    }

    public Map<String, String> getTags() {
        return new MultiMapLookup<>(rawStmtsBlock.getTags(), rawStmtsDoc.getTags());
    }

    public Map<String, Object> getParams() {
        return new MultiMapLookup<>(rawStmtsBlock.getParams(), rawStmtsDoc.getParams());
    }

    public Map<String, String> getParamsAsText() {
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawStmtsBlock.getParams(), rawStmtsDoc.getParams());
        LinkedHashMap<String, String> stringmap = new LinkedHashMap<>();
        lookup.forEach((k, v) -> stringmap.put(k, v.toString()));
        return stringmap;
    }

    @SuppressWarnings("unchecked")
    public <V> V getParamOrDefault(String name, V defaultValue) {
        Objects.requireNonNull(defaultValue);
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawStmtsBlock.getParams(), rawStmtsDoc.getParams());
        if (!lookup.containsKey(name)) {
            return defaultValue;
        }
        Object value = lookup.get(name);
        return (V) defaultValue.getClass().cast(value);
    }

    public <V> V getParam(String name, Class<? extends V> type) {
        MultiMapLookup<Object> lookup = new MultiMapLookup<>(rawStmtsBlock.getParams(), rawStmtsDoc.getParams());
        Object object = lookup.get(name);
        V value = type.cast(object);
        return value;
    }

    public Map<String, String> getBindings() {
        return new MultiMapLookup<>(rawStmtsBlock.getBindings(), rawStmtsDoc.getBindings());
    }

    @Override
    @NotNull
    public Iterator<OpTemplate> iterator() {
        return getOps().iterator();
    }
}
